package com.safinance.core.domain;

/**
 * Contrato abstrato para a entidade Usuário no sistema.
 */
public interface User extends Entity {
    String getName();
    String getEmail();
    boolean checkPassword(String password);
    boolean isAdmin();
    Role getRole();
}
