package com.safinance.core.domain;

public final class StandardTax implements TaxStrategy {
    private double rate;

    public StandardTax(double rate) {
        this.rate = rate;
    }

    @Override
    public double calculateTax(double amount) {
        return amount * rate;
    }
}
