package com.safinance.core.domain.tax;

/**
 * Defines how a transaction fee is calculated.
 */
public interface TaxStrategy {

    double calculateTax(double amount);
}