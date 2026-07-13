package com.safinance.core.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Mercado de ativos simulado em memória (Implementação POO).
 * 
 * Mantém um catálogo de ativos disponíveis e um mapa de preços em memória.
 * Os preços são movidos com variação aleatória escalada pela volatilidade.
 */
public class SimulatedAssetMarket implements Market {

    private final Random random = new Random();
    private static final int BLOCK_SECONDS = 10;

    private Instant lastMove = Instant.now();
    private Runnable onMove;

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
        currentPrices.put("FTX1", 85.00);
        currentPrices.put("VRPX", 134.30);
        currentPrices.put("NOVA11", 98.20);
        currentPrices.put("LUNA11", 72.50);
        currentPrices.put("PRA100", 1000.00);
        currentPrices.put("SIL200", 1000.00);
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
    public synchronized void advanceOneBlock() {
        for (Asset asset : availableAssets) {
            String ticker = asset.getTicker();
            double current = currentPrices.getOrDefault(ticker, 1.0);
            currentPrices.put(ticker, asset.calculateNextPrice(current));
        }
        registerMove(Instant.now());
    }

    private synchronized void catchUp(long blocks) {
        if (blocks <= 0) return;
        for (Asset asset : availableAssets) {
            String ticker = asset.getTicker();
            double current = currentPrices.getOrDefault(ticker, 1.0);
            currentPrices.put(ticker, round2(stepScaled(current, asset.getVolatility(), blocks)));
        }
        registerMove(lastMove.plusSeconds(blocks * BLOCK_SECONDS));
    }

    @Override
    public synchronized void catchUpToNow() {
        catchUp(blocksBetween(lastMove, Instant.now()));
    }

    @Override
    public synchronized void restoreState(Map<String, Double> prices, Instant lastMoveInstant) {
        if (prices != null) {
            for (var entry : prices.entrySet()) {
                if (entry.getValue() != null && currentPrices.containsKey(entry.getKey())) {
                    currentPrices.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (lastMoveInstant != null) {
            lastMove = lastMoveInstant;
        }
    }

    @Override
    public Instant lastMoveInstant() {
        return lastMove;
    }

    @Override
    public void setOnMove(Runnable callback) {
        onMove = callback;
    }

    private void registerMove(Instant instant) {
        lastMove = instant;
        Runnable callback = onMove;
        if (callback != null) {
            try {
                callback.run();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void refreshPricesAfterOperation() {
        advanceOneBlock();
    }

    private double stepScaled(double price, double volatility, long blocks) {
        double factor = 1.0 + random.nextGaussian() * volatility * Math.sqrt(blocks);
        return Math.max(1.0, price * factor);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private long blocksBetween(Instant from, Instant to) {
        long seconds = Duration.between(from, to).getSeconds();
        return Math.max(0, seconds / BLOCK_SECONDS);
    }

    @Override
    public synchronized Map<String, Double> snapshotPrices() {
        return Map.copyOf(currentPrices);
    }

    @Override
    public List<String> marketSummary() {
        Map<String, Double> snapshot = snapshotPrices();
        return availableAssets.stream()
            .map(asset -> String.format("%s - %s | R$ %.2f", asset.getTicker(), asset.getName(), snapshot.get(asset.getTicker())))
            .collect(Collectors.toList());
    }
}
