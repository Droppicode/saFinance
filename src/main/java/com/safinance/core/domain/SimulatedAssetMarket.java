package com.safinance.core.domain;

import java.util.List;
import java.util.Map;

/**
 * Mercado de ativos simulado em memória que delega as operações 
 * para o AssetMarket estático (implementando Live Refresh e Persistência).
 */
public class SimulatedAssetMarket implements Market {

    public SimulatedAssetMarket() {
    }

    @Override
    public List<Asset> availableAssets() {
        return AssetMarket.availableAssets();
    }

    @Override
    public Asset findByTicker(String ticker) {
        return AssetMarket.findByTicker(ticker);
    }

    @Override
    public Double priceFor(String ticker) {
        return AssetMarket.priceFor(ticker);
    }

    @Override
    public void refreshPricesAfterOperation() {
        AssetMarket.refreshPricesAfterOperation();
    }

    @Override
    public Map<String, Double> snapshotPrices() {
        return AssetMarket.snapshotPrices();
    }
}
