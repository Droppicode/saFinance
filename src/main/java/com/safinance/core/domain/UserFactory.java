package com.safinance.core.domain;

/**
 * Factory para criação de usuários.
 * Isola a lógica de gênese de objetos, escondendo as classes concretas.
 */
public class UserFactory {
    
    /**
     * Cria um usuário baseado na regra fornecida (ADMIN ou REGULAR).
     *
     * @param role O papel do usuário (ADMIN ou REGULAR)
     * @param id O ID do usuário
     * @param name O nome do usuário
     * @param email O e-mail do usuário
     * @param passwordHash A senha hasheada do usuário
     * @return A instância correta do usuário
     */
    public static User createUser(Role role, String id, String name, String email, String passwordHash) {
        if (role == Role.ADMIN) {
            return new AdminUser(id, name, email, passwordHash);
        } else {
            return new RegularUser(id, name, email, passwordHash);
        }
    }
}
