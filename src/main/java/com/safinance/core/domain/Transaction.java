package com.safinance.core.domain;
import java.time.LocalDateTime;
public interface Transaction extends Entity {
    double getAmount();
    LocalDateTime getDate();
    String getDescription();
    String getAccountId();
    
    /** Verifica se a transação pertence a uma conta, evitando train wrecks. */
    default boolean belongsToAccount(String accountId) {
        return getAccountId() != null && getAccountId().equals(accountId);
    }

    /** Indica se a transação é uma entrada (receita), evitando o uso de instanceof. */
    boolean isIncome();
