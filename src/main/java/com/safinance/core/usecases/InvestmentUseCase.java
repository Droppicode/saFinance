package com.safinance.core.usecases;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.Asset;
import com.safinance.core.domain.AssetMarket;
import com.safinance.core.domain.AssetPosition;
import com.safinance.core.domain.WalletAccount;
import com.safinance.core.domain.User;
import com.safinance.infra.persistence.Repository;

/**
 * Caso de uso para operações de investimento em WalletAccount.
 */
public class InvestmentUseCase {

    private final Repository<Account, String> accountRepository;

    public InvestmentUseCase(Repository<Account, String> accountRepository) {
        this.accountRepository = accountRepository;
    }

    public WalletAccount getWalletAccount(User user) {
        return accountRepository.findAll().stream()
            .filter(account -> account instanceof WalletAccount)
            .map(account -> (WalletAccount) account)
            .filter(account -> account.getOwnerId().equals(user.getId()))
            .findFirst()
            .orElse(null);
    }

    public WalletAccount buyAsset(WalletAccount wallet, Asset asset, double quantity, double pricePerUnit) {
        if (wallet == null) throw new IllegalArgumentException("Carteira não encontrada.");
        if (asset == null) throw new IllegalArgumentException("Ativo não pode ser nulo.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        if (pricePerUnit <= 0) throw new IllegalArgumentException("Preço por unidade deve ser maior que zero.");

        double totalCost = quantity * pricePerUnit;
        if (wallet.getBalance() < totalCost) {
            throw new IllegalArgumentException("Saldo insuficiente para comprar este ativo.");
        }

        Map<String, AssetPosition> newPortfolio = new HashMap<>(wallet.getPortfolio());
        var position = newPortfolio.get(asset.getTicker());
        if (position == null) {
            position = new AssetPosition(asset, quantity, pricePerUnit, LocalDateTime.now());
        } else {
            position = position.updatePosition(quantity, pricePerUnit);
        }
        newPortfolio.put(asset.getTicker(), position);

        WalletAccount updated = new WalletAccount(wallet.getId(), wallet.getOwnerId(), wallet.getBalance() - totalCost, newPortfolio);
        accountRepository.save(updated);
        AssetMarket.refreshPricesAfterOperation();
        return updated;
    }

    public WalletAccount sellAsset(WalletAccount wallet, String ticker, double quantity, double pricePerUnit) {
        if (wallet == null) throw new IllegalArgumentException("Carteira não encontrada.");
        if (ticker == null || ticker.isBlank()) throw new IllegalArgumentException("Ticker não pode ser vazio.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        if (pricePerUnit <= 0) throw new IllegalArgumentException("Preço por unidade deve ser maior que zero.");

        var position = wallet.getPortfolio().get(ticker);
        if (position == null) {
            throw new IllegalArgumentException("Ativo não encontrado no portfólio.");
        }

        AssetPosition updatedPosition = position.reducePosition(quantity);
        Map<String, AssetPosition> newPortfolio = new HashMap<>(wallet.getPortfolio());
        if (updatedPosition == null) {
            newPortfolio.remove(ticker);
        } else {
            newPortfolio.put(ticker, updatedPosition);
        }

        double proceeds = quantity * pricePerUnit;
        WalletAccount updated = new WalletAccount(wallet.getId(), wallet.getOwnerId(), wallet.getBalance() + proceeds, newPortfolio);
        accountRepository.save(updated);
        AssetMarket.refreshPricesAfterOperation();
        return updated;
    }

    public List<String> getPortfolioSummary(WalletAccount wallet) {
        if (wallet == null) return List.of();
        return wallet.getPortfolio().values().stream()
            .map(position -> String.format("%s x %.4f @ R$ %.2f (Preço Médio: R$ %.2f)",
                position.getAsset().getTicker(), position.getQuantity(), position.getAveragePrice(), position.getAveragePrice()))
            .toList();
    }
}
