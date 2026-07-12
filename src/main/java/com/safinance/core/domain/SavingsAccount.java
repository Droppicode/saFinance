package com.safinance.core.domain;

import java.time.YearMonth;

import com.safinance.core.exception.InsufficientFundsException;
import com.safinance.core.exception.InvalidTransactionException;

/**
 * Conta Poupança. 
 * Rende juros mensais emitidos pelo Banco Central.
 * Estritamente imutável (Padrão Wither).
 */
public class SavingsAccount implements Account {
    private final String id;
    private final String ownerId;
    private final double balance;

    public SavingsAccount(String id, String ownerId, double balance) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("O ID da conta não pode ser nulo.");
        if (ownerId == null || ownerId.isBlank()) throw new IllegalArgumentException("O ID do dono não pode ser nulo.");
        if (!Double.isFinite(balance)) throw new IllegalArgumentException("O saldo da conta deve ser finito.");
        if (balance < 0) throw new IllegalArgumentException("O saldo inicial da poupança não pode ser negativo.");
        
        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getOwnerId() { return ownerId; }

    @Override
    public String getAccountType() { return "Poupança"; }
    
    @Override
    public double getBalance() { return balance; }

    @Override
    public String getDisplaySummary() {
        return String.format("%-12s | %-10.2f | %-10s", getAccountType(), getBalance(), "-");
    }

    @Override
    public SavingsAccount process(Transaction t) {
        validateTransaction(t);

        double newBalance = this.balance + t.getAmount();
        
        if (!Double.isFinite(newBalance)) {
            throw new InvalidTransactionException("The resulting balance must be finite.");
        }

        if (newBalance < 0) {
            throw new InsufficientFundsException("Insufficient balance in SavingsAccount.");
        }
        
        return new SavingsAccount(this.id, this.ownerId, newBalance);
    }

    /**
     * Aplica o rendimento mensal usando os dados da instituição bancária.
     */
    public SavingsAccount applyMonthlyYield(YearMonth month, Bank bank) {

        if (month == null) {
            throw new IllegalArgumentException("O mês não pode ser nulo.");
        }

        if (bank == null) {
            throw new IllegalArgumentException("O banco não pode ser nulo.");
        }

        double rate = bank.getYieldRate(month);

        if (!Double.isFinite(rate) || rate < 0) {
            throw new IllegalArgumentException("A taxa de rendimento deve ser finita e não negativa.");
        }

        double yieldAmount = this.balance * rate;
        double newBalance = this.balance + yieldAmount;

        if (!Double.isFinite(newBalance)) {
            throw new IllegalArgumentException("O saldo resultante do rendimento deve ser finito.");
        }
        
        return new SavingsAccount(this.id, this.ownerId, newBalance);
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new InvalidTransactionException("Transaction cannot be null.");
        }

        if (!this.id.equals(transaction.getAccountId())) {
            throw new InvalidTransactionException("Transaction does not belong to this account.");
        }
    }
}