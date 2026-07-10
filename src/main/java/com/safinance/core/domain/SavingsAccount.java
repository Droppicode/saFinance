package com.safinance.core.domain;

import java.time.YearMonth;

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
        
        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getOwnerId() { return ownerId; }

    @Override
    public double getBalance() { return balance; }

    @Override
    public SavingsAccount process(Transaction t) {
        double newBalance = this.balance + t.getAmount();
        
        if (newBalance < 0) {
            throw new IllegalArgumentException("Saldo insuficiente na Poupança.");
        }
        
        return new SavingsAccount(this.id, this.ownerId, newBalance);
    }

    /**
     * Aplica o rendimento mensal usando os dados da instituição bancária.
     */
    public SavingsAccount applyMonthlyYield(YearMonth month, Bank bank) {
        double rate = bank.getYieldRate(month);
        double yieldAmount = this.balance * rate;
        
        return new SavingsAccount(this.id, this.ownerId, this.balance + yieldAmount);
    }
}
