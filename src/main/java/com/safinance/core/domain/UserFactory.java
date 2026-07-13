package com.safinance.core.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    public static User createUser(Role role, String id, String name, String email, String password) {
        String hashed = hashPassword(password);
        if (role == Role.ADMIN) {
            return new AdminUser(id, name, email, hashed);
        } else {
            return new RegularUser(id, name, email, hashed);
        }
    }

    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
