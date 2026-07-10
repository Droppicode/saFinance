package com.safinance.core.domain;

/**
 * Interface base para todas as contas do sistema.
 */
public interface Account extends Entity {
    String getOwnerId();
    double getBalance();
    
    /**
     * Padrão Wither: Processa uma transação e retorna uma NOVA conta com o saldo atualizado.
     * Não altera a conta atual em memória.
     */
    Account process(Transaction t);
}
