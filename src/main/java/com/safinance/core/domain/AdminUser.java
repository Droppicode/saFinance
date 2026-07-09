package com.safinance.core.domain;

import java.util.Objects;

/**
 * Entidade de domínio estritamente imutável representando um Administrador do sistema.
 */
public class AdminUser implements User {
    private final String id;
    private final String name;
    private final String email;
    private final String passwordHash;

    public AdminUser(String id, String name, String email, String passwordHash) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("O ID não pode ser nulo ou vazio.");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("O e-mail não pode ser nulo.");
        
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public String getEmail() { return email; }

    @Override
    public boolean checkPassword(String password) {
        return Objects.equals(this.passwordHash, password);
    }

    @Override
    public boolean isAdmin() { return true; }

    @Override
    public Role getRole() { return Role.ADMIN; }
}
