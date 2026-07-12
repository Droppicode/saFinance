package com.safinance.core.usecases;

import java.util.List;

import com.safinance.core.domain.AdminUser;
import com.safinance.core.domain.RegularUser;
import com.safinance.core.domain.Role;
import com.safinance.core.domain.User;
import com.safinance.infra.persistence.Repository;

/**
 * Caso de uso para gerenciar usuários.
 */
public class UserUseCase {

    // A dependência fica declarada, mas a implementação concreta é desconhecida.
    private final Repository<User, String> userRepository;

    /**
     * Injeção de Dependência via Construtor.
     */
    public UserUseCase(Repository<User, String> userRepository) {
        if (userRepository == null) throw new IllegalArgumentException("O repositório de usuários não pode ser nulo.");
        this.userRepository = userRepository;
    }

    public User getUser(String id) {
        // Uso direto da dependência, sem acessar Singletons globais.
        return userRepository.findById(id);
    }
    
    /**
     * Cria um novo usuário.
     *
     * @param name O nome do usuário.
     * @param email O email do usuário.
     * @param password A senha do usuário.
     * @param role A função do usuário.
     * @return O usuário criado.
     */
    public User createUser(String name, String email, String password, Role role) {
        // Uso direto da dependência, sem acessar Singletons globais.
        if (role == Role.ADMIN) {
            User adminUser = new AdminUser(email, name, email, password);
            userRepository.save(adminUser);
            return adminUser;
        } else {
            User regularUser = new RegularUser(email, name, email, password);
            userRepository.save(regularUser);
            return regularUser;
        }
    }

    /**
     * Recupera todos os usuários.
     * @return Uma lista de todos os usuários.
     */
    public List<User> getAllUsers() {
        // Uso direto da dependência, sem acessar Singletons globais.
        return userRepository.findAll();
    }

}
