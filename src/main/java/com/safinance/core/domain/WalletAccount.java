package com.safinance.core.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Conta corrente/carteira principal do usuário.
 * Armazena o saldo disponível e o portfólio de investimentos.
 * Estritamente imutável (Padrão Wither).
 */
public class WalletAccount implements Account {
    private final String id;
    private final String ownerId;
    private final double balance;
    private final Map<String, AssetPosition> portfolio;

    public WalletAccount(String id, String ownerId, double balance, Map<String, AssetPosition> portfolio) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("O ID da conta não pode ser nulo.");
        if (ownerId == null || ownerId.isBlank()) throw new IllegalArgumentException("O ID do dono não pode ser nulo.");
        
        this.id = id;
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
    public double getBalance() { return balance; }

    public Map<String, AssetPosition> getPortfolio() { return portfolio; }

    @Override
    public WalletAccount process(Transaction t) {
        double newBalance = this.balance + t.getAmount();
        
        if (newBalance < 0) {
            throw new IllegalArgumentException("Saldo insuficiente na Wallet.");
        }
        
        return new WalletAccount(this.id, this.ownerId, newBalance, this.portfolio);
    }

    /**
     * Wither para atualizar a carteira de investimentos.
     */
    public WalletAccount withPortfolio(Map<String, AssetPosition> newPortfolio) {
        return new WalletAccount(this.id, this.ownerId, this.balance, newPortfolio);
    }
}
