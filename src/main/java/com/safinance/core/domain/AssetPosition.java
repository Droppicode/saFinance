package com.safinance.core.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa uma posição de ativo dentro do portfólio da WalletAccount.
 */
public class AssetPosition {
    private final Asset asset;
    private final double quantity;
    private final double averagePrice;
    private final LocalDateTime firstPurchaseDate;

    public AssetPosition(Asset asset, double quantity, double averagePrice, LocalDateTime firstPurchaseDate) {
        if (asset == null) throw new IllegalArgumentException("Ativo não pode ser nulo.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        if (averagePrice <= 0) throw new IllegalArgumentException("Preço médio deve ser maior que zero.");
        if (firstPurchaseDate == null) throw new IllegalArgumentException("Data da primeira compra não pode ser nula.");
        this.asset = asset;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.firstPurchaseDate = firstPurchaseDate;
    }

    public Asset getAsset() {
        return asset;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public LocalDateTime getFirstPurchaseDate() {
        return firstPurchaseDate;
    }

    public AssetPosition updatePosition(double additionalQuantity, double purchasePrice) {
        if (additionalQuantity <= 0) throw new IllegalArgumentException("Quantidade adicional deve ser maior que zero.");
        if (purchasePrice <= 0) throw new IllegalArgumentException("Preço de compra deve ser maior que zero.");

        double totalCost = this.averagePrice * this.quantity + purchasePrice * additionalQuantity;
        double newQuantity = this.quantity + additionalQuantity;
        double newAveragePrice = totalCost / newQuantity;

        return new AssetPosition(asset, newQuantity, newAveragePrice, firstPurchaseDate);
    }

    public AssetPosition reducePosition(double quantityToSell) {
        if (quantityToSell <= 0) throw new IllegalArgumentException("Quantidade vendida deve ser maior que zero.");
        if (quantityToSell > this.quantity) {
            throw new IllegalArgumentException("Quantidade a vender maior que a posição disponível.");
        }

        double remainingQuantity = this.quantity - quantityToSell;
        if (remainingQuantity == 0) {
            return null;
        }

        return new AssetPosition(asset, remainingQuantity, averagePrice, firstPurchaseDate);
    }

    /**
     * Formata a posição atual para exibição em extratos (Tell, Don't Ask).
     */
    public String getDisplaySummary() {
        return String.format("%s x %.4f @ R$ %.2f (Preço Médio: R$ %.2f)",
                asset.getTicker(), quantity, averagePrice, averagePrice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetPosition other)) return false;
        return Double.compare(other.quantity, quantity) == 0
            && Double.compare(other.averagePrice, averagePrice) == 0
            && Objects.equals(asset, other.asset)
            && Objects.equals(firstPurchaseDate, other.firstPurchaseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asset, quantity, averagePrice, firstPurchaseDate);
    }
}
