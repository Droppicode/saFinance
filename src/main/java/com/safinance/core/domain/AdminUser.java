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
        if (name == null || name.isBlank()) throw new IllegalArgumentException("O nome não pode ser nulo ou vazio.");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("O e-mail não pode ser nulo.");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("A senha não pode ser nula ou vazia.");
        
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
    public Role getRole() { return Role.ADMIN; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminUser that = (AdminUser) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
