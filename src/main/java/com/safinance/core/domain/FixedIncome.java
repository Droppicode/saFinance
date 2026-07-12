package com.safinance.core.domain;

public class FixedIncome extends BaseAsset {

    private final double fixedYield;

    public FixedIncome(String id, String ticker, String name, double fixedYield) {
        super(id, ticker, name);
        if (fixedYield < 0) throw new IllegalArgumentException("Rentabilidade fixa não pode ser negativa.");
        this.fixedYield = fixedYield;
    }

    public double getFixedYield() {
        return fixedYield;
    }
}
