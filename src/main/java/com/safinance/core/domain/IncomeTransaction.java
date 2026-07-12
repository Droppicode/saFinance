package com.safinance.core.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import com.safinance.core.exception.InvalidTransactionException;

/**
 * Represents an incoming transaction that increases an account balance.
 */
public final class IncomeTransaction implements Transaction {

    private final String id;
    private final double amount;
    private final LocalDateTime date;
    private final String description;
    private final String accountId;

    public IncomeTransaction(
            String id,
            double amount,
            LocalDateTime date,
            String description,
            String accountId
    ) {
        validateId(id);
        validateAmount(amount);
        validateDate(date);
        validateDescription(description);
        validateAccountId(accountId);

        this.id = id;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.accountId = accountId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getAccountId() {
        return accountId;
    }

    @Override
    public boolean isIncome() {
        return true;
    }

    private static void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidTransactionException(
                    "Transaction ID cannot be null or blank."
            );
        }
    }

    private static void validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new InvalidTransactionException(
                    "Income transaction amount must be finite and greater than zero."
            );
        }
    }

    private static void validateDate(LocalDateTime date) {
        if (date == null) {
            throw new InvalidTransactionException(
                    "Transaction date cannot be null."
            );
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidTransactionException(
                    "Transaction description cannot be null or blank."
            );
        }
    }

    private static void validateAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new InvalidTransactionException(
                    "Account ID cannot be null or blank."
            );
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof IncomeTransaction other)) {
            return false;
        }

        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}