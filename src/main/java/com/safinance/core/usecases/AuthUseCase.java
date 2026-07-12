package com.safinance.core.usecases;

import com.safinance.core.domain.User;
import com.safinance.infra.persistence.Repository;

/**
 * Caso de Uso responsável pela Autenticação.
 */
public class AuthUseCase {
    
    // A dependência fica declarada, mas a implementação concreta é desconhecida.
    private final Repository<User, String> userRepository;

    /**
     * Injeção de Dependência via Construtor.
     */
    public AuthUseCase(Repository<User, String> userRepository) {
        if (userRepository == null) throw new IllegalArgumentException("O repositório de usuários não pode ser nulo.");
        this.userRepository = userRepository;
    }

    public User login(String email, String password) {
        // Uso direto da dependência, sem acessar Singletons globais.
        User user = userRepository.findById(email);
        
        if (user != null && user.checkPassword(password)) {
            return user; // Login bem-sucedido
        }
        
        throw new IllegalArgumentException("Credenciais inválidas."); // Fail-Fast
    }
}
