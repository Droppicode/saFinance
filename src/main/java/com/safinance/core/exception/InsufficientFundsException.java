package com.safinance.core.exception;

/**
 * Thrown when an account cannot complete a debit operation
 * because its available balance or credit limit is insufficient.
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}