package com.safinance.core.domain;

/**
 * Interface base para todas as contas do sistema.
 * 
 * Nota de Arquitetura: Por design (e devido ao modelo de persistência append-only do JsonlRepository),
 * as contas NÃO armazenam internamente uma List<Transaction>. Fazer isso causaria um crescimento 
 * exponencial (O(N²)) do arquivo de dados da conta a cada snapshot salvo. Em vez disso, 
 * as transações são persistidas separadamente e as agregações são feitas via UseCase.
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
