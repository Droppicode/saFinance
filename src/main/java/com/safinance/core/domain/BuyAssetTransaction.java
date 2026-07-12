package com.safinance.core.domain;

import java.time.LocalDateTime;

public class BuyAssetTransaction implements Transaction {
    private final String id;
    private final double amount;
    private final LocalDateTime date;
    private final String accountId;
    private final Asset asset;
    private final double quantity;
    private final double pricePerUnit;

    public BuyAssetTransaction(String id, LocalDateTime date, String accountId, Asset asset, double quantity, double pricePerUnit) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID cannot be null.");
        if (date == null) throw new IllegalArgumentException("Date cannot be null.");
        if (accountId == null || accountId.isBlank()) throw new IllegalArgumentException("Account ID cannot be null.");
        if (asset == null) throw new IllegalArgumentException("Asset cannot be null.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        if (pricePerUnit <= 0) throw new IllegalArgumentException("Price must be positive.");

        this.id = id;
        this.amount = -(quantity * pricePerUnit); // Compra reduz o saldo
        this.date = date;
        this.accountId = accountId;
        this.asset = asset;
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
        return String.format("Compra %.4f %s @ R$ %.2f", quantity, asset.getTicker(), pricePerUnit);
    }

    @Override
    public String getAccountId() { return accountId; }

    @Override
    public boolean isIncome() { return false; }

    @Override
    public boolean isTransfer() { return false; }

    public Asset getAsset() { return asset; }
    public String getAssetTicker() { return asset.getTicker(); }
    public String getAssetName() { return asset.getName(); }
    public double getQuantity() { return quantity; }
    public double getPricePerUnit() { return pricePerUnit; }
}
