package com.safinance.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Creates transaction instances with generated identity and timestamp.
 */
public final class TransactionFactory {

    public IncomeTransaction createIncome(
            double amount,
            String description,
            String accountId
    ) {
        return new IncomeTransaction(
                UUID.randomUUID().toString(),
                amount,
                LocalDateTime.now(),
                description,
                accountId
        );
    }

    public ExpenseTransaction createExpense(
            double amount,
            String description,
            String accountId
    ) {
        return new ExpenseTransaction(
                UUID.randomUUID().toString(),
                amount,
                LocalDateTime.now(),
                description,
                accountId
        );
    }

    public BuyAssetTransaction createBuyAsset(Asset asset, double quantity, double pricePerUnit, String accountId) {
        return new BuyAssetTransaction(UUID.randomUUID().toString(), LocalDateTime.now(), accountId, asset, quantity, pricePerUnit);
    }

    public SellAssetTransaction createSellAsset(String ticker, double quantity, double pricePerUnit, String accountId) {
        return new SellAssetTransaction(UUID.randomUUID().toString(), LocalDateTime.now(), accountId, ticker, quantity, pricePerUnit);
    }
}