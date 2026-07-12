package com.safinance.core.usecases;

import java.util.List;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.Asset;
import com.safinance.core.domain.Market;
import com.safinance.core.domain.WalletAccount;
import com.safinance.core.domain.User;
import com.safinance.core.domain.Transaction;
import com.safinance.core.domain.TransactionFactory;
import com.safinance.infra.persistence.Repository;

/**
 * Caso de uso para operações de investimento em WalletAccount.
 */
public class InvestmentUseCase {

    private final Repository<Account, String> accountRepository;
    private final Repository<Transaction, String> transactionRepository;
    private final TransactionFactory transactionFactory;
    private final Market market;

    public InvestmentUseCase(Repository<Account, String> accountRepository, Repository<Transaction, String> transactionRepository, TransactionFactory transactionFactory, Market market) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionFactory = transactionFactory;
        this.market = market;
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

        Transaction tx = transactionFactory.createBuyAsset(asset, quantity, pricePerUnit, wallet.getId());
        WalletAccount updated = (WalletAccount) wallet.process(tx);

        accountRepository.save(updated);
        transactionRepository.save(tx);
        market.refreshPricesAfterOperation();
        return updated;
    }

    public WalletAccount sellAsset(WalletAccount wallet, String ticker, double quantity, double pricePerUnit) {
        if (wallet == null) throw new IllegalArgumentException("Carteira não encontrada.");
        if (ticker == null || ticker.isBlank()) throw new IllegalArgumentException("Ticker não pode ser vazio.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        if (pricePerUnit <= 0) throw new IllegalArgumentException("Preço por unidade deve ser maior que zero.");

        Transaction tx = transactionFactory.createSellAsset(ticker, quantity, pricePerUnit, wallet.getId());
        WalletAccount updated = (WalletAccount) wallet.process(tx);

        accountRepository.save(updated);
        transactionRepository.save(tx);
        market.refreshPricesAfterOperation();
        return updated;
    }

    public List<String> getPortfolioSummary(WalletAccount wallet) {
        if (wallet == null) return List.of();
        return wallet.getPortfolio().values().stream()
            .map(position -> String.format("%s x %.4f @ R$ %.2f (Preço Médio: R$ %.2f)",
                position.getAsset().getTicker(), position.getQuantity(), position.getAveragePrice(), position.getAveragePrice()))
            .toList();
    }

    public Market getMarket() {
        return market;
    }
}
