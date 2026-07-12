package com.safinance.core.domain;

import java.time.LocalDateTime;

public class SellAssetTransaction implements Transaction {
    private final String id;
    private final double amount;
    private final LocalDateTime date;
    private final String accountId;
    private final String ticker;
    private final double quantity;
    private final double pricePerUnit;

    public SellAssetTransaction(String id, LocalDateTime date, String accountId, String ticker, double quantity, double pricePerUnit) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID cannot be null.");
        if (date == null) throw new IllegalArgumentException("Date cannot be null.");
        if (accountId == null || accountId.isBlank()) throw new IllegalArgumentException("Account ID cannot be null.");
        if (ticker == null || ticker.isBlank()) throw new IllegalArgumentException("Ticker cannot be null.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        if (pricePerUnit <= 0) throw new IllegalArgumentException("Price must be positive.");

        this.id = id;
        this.amount = quantity * pricePerUnit; // Venda aumenta o saldo
        this.date = date;
        this.accountId = accountId;
        this.ticker = ticker;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    @Override
    public String getId() { return id; }

    @Override
    public double getAmount() { return amount; }

    @Override
    public LocalDateTime getDate() { return date; }

    @Override
    public String getDescription() {
        return String.format("Venda %.4f %s @ R$ %.2f", quantity, ticker, pricePerUnit);
    }

    @Override
    public String getAccountId() { return accountId; }

    @Override
    public boolean isIncome() { return true; }

    @Override
    public boolean isTransfer() { return false; }

    public String getTicker() { return ticker; }
    public double getQuantity() { return quantity; }
    public double getPricePerUnit() { return pricePerUnit; }
}
