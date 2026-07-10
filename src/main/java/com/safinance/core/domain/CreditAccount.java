package com.safinance.core.domain;

/**
 * Conta de Crédito. Pode ter saldo negativo até o limite pré-estabelecido.
 * Estritamente imutável (Padrão Wither).
 */
public class CreditAccount implements Account {
    private final String id;
    private final String ownerId;
    private final double balance;
    private final double creditLimit;

    public CreditAccount(String id, String ownerId, double balance, double creditLimit) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("O ID da conta não pode ser nulo.");
        if (ownerId == null || ownerId.isBlank()) throw new IllegalArgumentException("O ID do dono não pode ser nulo.");
        
        this.id = id;
        this.ownerId = ownerId;
        this.balance = balance;
        this.creditLimit = creditLimit;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getOwnerId() { return ownerId; }

    @Override
    public double getBalance() { return balance; }
    
    public double getCreditLimit() { return creditLimit; }

    @Override
    public CreditAccount process(Transaction t) {
        double newBalance = this.balance + t.getAmount(); // Para crédito, t.getAmount() de uma compra será negativo
        
        // Verifica se a dívida ultrapassou o limite
        if (newBalance < -creditLimit) {
            throw new IllegalArgumentException("Limite de crédito excedido.");
        }
        
        return new CreditAccount(this.id, this.ownerId, newBalance, this.creditLimit);
    }
}
