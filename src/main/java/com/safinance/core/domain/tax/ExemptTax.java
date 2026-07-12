package com.safinance.core.domain.tax;

/**
 * Represents a transaction with no applicable fee.
 */
public final class ExemptTax implements TaxStrategy {

    @Override
    public double calculateTax(double amount) {
        validateAmount(amount);
        return 0.0;
    }

    private static void validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be finite and greater than zero."
            );
        }
    }
}