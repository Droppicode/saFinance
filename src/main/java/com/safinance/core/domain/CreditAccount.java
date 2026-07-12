package com.safinance.core.domain;

import com.safinance.core.exception.InsufficientFundsException;
import com.safinance.core.exception.InvalidTransactionException;

/**
 * Conta de Crédito. Pode ter saldo negativo até o limite pré-estabelecido.
 * Estritamente imutável (Padrão Wither).
 */
public class CreditAccount implements Account {
    private final String id;
    private final String name;
    private final String ownerId;
    private final double balance;
    private final double creditLimit;

    public CreditAccount(String id, String ownerId, double balance, double creditLimit, String name) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("O ID da conta não pode ser nulo.");
        if (ownerId == null || ownerId.isBlank()) throw new IllegalArgumentException("O ID do dono não pode ser nulo.");
        if (!Double.isFinite(balance)) throw new IllegalArgumentException("O saldo da conta deve ser finito.");
        if (!Double.isFinite(creditLimit) || creditLimit < 0) throw new IllegalArgumentException("O limite de crédito deve ser finito e não negativo.");
        if (balance < -creditLimit) throw new IllegalArgumentException("O saldo inicial não pode ultrapassar o limite de crédito.");
        
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.balance = balance;
        this.creditLimit = creditLimit;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getOwnerId() { return ownerId; }

    @Override
    public String getName() { return name; }

    @Override
    public double getBalance() { return balance; }
    
    public double getCreditLimit() { return creditLimit; }
    
    @Override
    public String getAccountType() { return "Crédito"; }

    @Override
    public String getDisplaySummary() {
        return String.format("%-15s | %-12s | %-10.2f | %-10.2f", getName(), getAccountType(), getBalance(), getCreditLimit());
    }

    @Override
    public CreditAccount process(Transaction t) {
        validateTransaction(t);

        double newBalance = this.balance + t.getAmount(); // Para crédito, t.getAmount() de uma compra será negativo
        
        if (!Double.isFinite(newBalance)) {
            throw new InvalidTransactionException("The resulting balance must be finite.");
        }

        // Verifica se a dívida ultrapassou o limite
        if (newBalance < -creditLimit) {
            throw new InsufficientFundsException("Credit limit exceeded.");
        }
        
        return new CreditAccount(this.id, this.ownerId, newBalance, this.creditLimit, this.name);
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new InvalidTransactionException(
                    "Transaction cannot be null."
            );
        }

        if (!this.id.equals(transaction.getAccountId())) {
            throw new InvalidTransactionException(
                    "Transaction does not belong to this account."
            );
        }
    }
}