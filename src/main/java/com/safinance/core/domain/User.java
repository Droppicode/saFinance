package com.safinance.core.domain;

/**
 * Contrato abstrato para a entidade Usuário no sistema.
 */
public interface User extends Entity {
    String getName();
    String getEmail();
    boolean checkPassword(String password);
    default boolean isAdmin() {
        return getRole() == Role.ADMIN;
    }
    Role getRole();
}
