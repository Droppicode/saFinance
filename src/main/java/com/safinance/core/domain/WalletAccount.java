package com.safinance.core.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.safinance.core.exception.InvalidTransactionException;
import com.safinance.core.exception.InsufficientFundsException;

/**
 * Conta corrente/carteira principal do usuário.
 * Armazena o saldo disponível e o portfólio de investimentos.
 * Estritamente imutável (Padrão Wither).
 */
public class WalletAccount implements Account {
    private final String id;
    private final String name;
    private final String ownerId;
    private final double balance;
    private final Map<String, AssetPosition> portfolio;

    public WalletAccount(String id, String ownerId, double balance, Map<String, AssetPosition> portfolio, String name) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("O ID da conta não pode ser nulo.");
        if (ownerId == null || ownerId.isBlank()) throw new IllegalArgumentException("O ID do dono não pode ser nulo.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("O nome da conta não pode ser nulo.");
        if (!Double.isFinite(balance)) throw new IllegalArgumentException("O saldo da conta deve ser finito.");
        if (balance < 0) throw new IllegalArgumentException("O saldo inicial da Wallet não pode ser negativo.");
        
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.balance = balance;
        // Garantindo que a lista (Map) de portfólio seja blindada e imutável
        this.portfolio = portfolio == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(portfolio));
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getOwnerId() { return ownerId; }

    @Override
    public String getName() { return name; }

    @Override
    public String getAccountType() { return "Carteira"; }

    @Override
    public String getDisplaySummary() {
        return String.format("%-15s | %-12s | %-10.2f | %-10s", getName(), getAccountType(), getBalance(), "-");
    }

    @Override
    public double getBalance() { return balance; }

    public Map<String, AssetPosition> getPortfolio() {
        return portfolio == null ? Collections.emptyMap() : portfolio;
    }

    /**
     * Retorna a representação do portfólio já formatada, mantendo o encapsulamento (Tell, Don't Ask).
     */
    public java.util.List<String> getPortfolioSummary() {
        return getPortfolio().values().stream()
                .map(AssetPosition::getDisplaySummary)
                .toList();
    }

    @Override
    public WalletAccount process(Transaction t) {
        validateTransaction(t);

        double newBalance = this.balance + t.getAmount();
        
        if (!Double.isFinite(newBalance)) {
            throw new InvalidTransactionException("The resulting balance must be finite.");
        }

        if (newBalance < 0) {
            throw new InsufficientFundsException("Insufficient balance in WalletAccount.");
        }
        
        Map<String, AssetPosition> newPortfolio = new HashMap<>(getPortfolio());

        switch (t) {
            case BuyAssetTransaction buyTx -> {
                var position = newPortfolio.get(buyTx.getAssetTicker());
                if (position == null) {
                    position = new AssetPosition(buyTx.getAsset(), buyTx.getQuantity(), buyTx.getPricePerUnit(), t.getDate());
                } else {
                    position = position.updatePosition(buyTx.getQuantity(), buyTx.getPricePerUnit());
                }
                newPortfolio.put(buyTx.getAssetTicker(), position);
            }
            case SellAssetTransaction sellTx -> {
                var position = newPortfolio.get(sellTx.getTicker());
                if (position == null) {
                    throw new InvalidTransactionException("Ativo não encontrado no portfólio.");
                }
                java.util.Optional<AssetPosition> updatedPositionOpt = position.reducePosition(sellTx.getQuantity());
                if (updatedPositionOpt.isEmpty()) {
                    newPortfolio.remove(sellTx.getTicker());
                } else {
                    newPortfolio.put(sellTx.getTicker(), updatedPositionOpt.get());
                }
            }
            default -> {
                // Outras transações não afetam o portfólio diretamente (apenas o saldo)
            }
        }
        
        return new WalletAccount(this.id, this.ownerId, newBalance, newPortfolio, this.name);
    }



    /**
     * Wither para atualizar a carteira de investimentos.
     */
    public WalletAccount withPortfolio(Map<String, AssetPosition> newPortfolio) {
        return new WalletAccount(this.id, this.ownerId, this.balance, newPortfolio, this.name);
    }

    public WalletAccount withBalance(double balance) {
        return new WalletAccount(this.id, this.ownerId, balance, this.portfolio, this.name);
    }
}