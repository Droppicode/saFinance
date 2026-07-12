package com.safinance.core.exception;

/**
 * Thrown when a transaction violates domain invariants.
 */
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}