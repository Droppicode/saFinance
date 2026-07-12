package com.safinance.core.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mercado de ativos simulado em memória.
 */
public class SimulatedAssetMarket implements Market {

    private final List<Asset> availableAssets;
    private final Map<String, Double> currentPrices;

    public SimulatedAssetMarket() {
        this.availableAssets = List.of(
            new Stock("FTX1", "FTX1", "Fictitious Tech", "Ficticious Tech Co.", 0.15),
            new Stock("VRPX", "VRPX", "Vertex Dynamics", "Vertex Dynamics & Cia.", 0.12),
            new RealEstateFund("NOVA11", "NOVA11", "Fundo Imobiliário Nova", "Logística", 0.10),
            new RealEstateFund("LUNA11", "LUNA11", "Fundo Imobiliário Luna", "Papel", 0.14),
            new FixedIncome("PRA100", "PRA100", "Prazo Certo 100 Dias", 0.065, 0.02),
            new FixedIncome("SIL200", "SIL200", "Série Silvestre 200 Dias", 0.055, 0.02)
        );

        this.currentPrices = new HashMap<>();
        Map<String, Double> basePrices = Map.of(
            "FTX1", 85.00,
            "VRPX", 134.30,
            "NOVA11", 98.20,
            "LUNA11", 72.50,
            "PRA100", 1000.00,
            "SIL200", 1000.00
        );

        for (Asset asset : availableAssets) {
            currentPrices.put(asset.getTicker(), basePrices.getOrDefault(asset.getTicker(), 1.0));
        }
    }

    @Override
    public List<Asset> availableAssets() {
        return availableAssets;
    }

    @Override
    public Asset findByTicker(String ticker) {
        return availableAssets.stream()
            .filter(asset -> asset.getTicker().equalsIgnoreCase(ticker))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Double priceFor(String ticker) {
        if (ticker == null) return null;
        return currentPrices.get(ticker.toUpperCase());
    }

    @Override
    public void refreshPricesAfterOperation() {
        for (Asset asset : availableAssets) {
            String ticker = asset.getTicker();
            Double current = currentPrices.get(ticker);
            Double nextPrice = asset.calculateNextPrice(current);
            currentPrices.put(ticker, nextPrice);
        }
    }

    @Override
    public Map<String, Double> snapshotPrices() {
        return Map.copyOf(currentPrices);
    }
}
