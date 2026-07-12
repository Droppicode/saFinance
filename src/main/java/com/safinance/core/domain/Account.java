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
    String getName();
    
    /** Verifica se a conta pertence a um determinado usuário (evita train wrecks). */
    default boolean isOwnedBy(String ownerId) {
        return getOwnerId() != null && getOwnerId().equals(ownerId);
    }
    
    /** Verifica se duas contas pertencem ao mesmo usuário. */
    default boolean isOwnedBySameUserAs(Account other) {
        return other != null && this.isOwnedBy(other.getOwnerId());
    }
    double getBalance();
    
    /** Retorna o tipo de conta amigável (ex: "Poupança"). Evita uso de getClass().getSimpleName(). */
    String getAccountType();

    /** 
     * Retorna o resumo formatado da conta para listagem (Tipo | Saldo | Limite). 
     * Evita violação de polimorfismo com 'instanceof' nos Menus.
     */
    String getDisplaySummary();
    
    /**
     * Padrão Wither: Processa uma transação e retorna uma NOVA conta com o saldo atualizado.
     * Não altera a conta atual em memória.
     */
    Account process(Transaction t);
}
