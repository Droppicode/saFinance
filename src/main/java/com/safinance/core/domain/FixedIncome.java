package com.safinance.core.domain;

public class FixedIncome extends BaseAsset {

    private final double fixedYield;

    public FixedIncome(String id, String ticker, String name, double fixedYield, double volatility) {
        super(id, ticker, name, volatility);
        if (fixedYield < 0) throw new IllegalArgumentException("Rentabilidade fixa não pode ser negativa.");
        this.fixedYield = fixedYield;
    }

    public double getFixedYield() {
        return fixedYield;
    }

    @Override
    public double calculateNextPrice(double currentPrice) {
        double rate = fixedYield / 252.0; // Assume annual rate spread over 252 simulated ticks
        double baseIncrease = currentPrice * (1.0 + rate);
        double randomFactor = 1.0 + (RANDOM.nextGaussian() * volatility);
        double nextPrice = baseIncrease * randomFactor;
        return Math.round(nextPrice * 100.0) / 100.0;
    }
}
