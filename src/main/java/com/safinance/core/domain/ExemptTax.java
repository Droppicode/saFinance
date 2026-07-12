package com.safinance.core.domain;

public final class ExemptTax implements TaxStrategy {
    @Override
    public double calculateTax(double amount) {
        return 0;
    }
}
