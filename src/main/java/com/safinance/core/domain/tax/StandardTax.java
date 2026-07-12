package com.safinance.core.domain.tax;

/**
 * Calculates a percentage-based transaction fee.
 */
public final class StandardTax implements TaxStrategy {

    private final double rate;

    public StandardTax(double rate) {
        if (!Double.isFinite(rate) || rate < 0) {
            throw new IllegalArgumentException(
                    "Tax rate must be finite and non-negative."
            );
        }

        this.rate = rate;
    }

    @Override
    public double calculateTax(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be finite and greater than zero."
            );
        }

        return amount * rate;
    }

    public double getRate() {
        return rate;
    }
}