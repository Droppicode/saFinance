package com.safinance.view;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.UserUseCase;

/**
 * Menu de login para a aplicação.
 */
public class LoginMenu extends BaseMenu {

    private final AuthUseCase authUseCase;
    private final UserUseCase userUseCase;
    private final AccountUseCase accountUseCase;

    // Construtor da classe.
    public LoginMenu(AuthUseCase authUseCase, UserUseCase userUseCase, AccountUseCase accountUseCase) {
        this.authUseCase = authUseCase;
        this.userUseCase = userUseCase;
        this.accountUseCase = accountUseCase;
    }

    /**
     * Exibe o menu de login e retorna o próximo estado.
     */
    @Override
    public BaseMenu render() {
        printHeader("Login");
        System.out.print("Email: ");
        String email = scanner.next();
        System.out.print("Senha: ");
        String password = scanner.next();

        try { // Fail-Fast: tenta autenticar e falha imediatamente se houver erro

            if (email.equals("teste")) { // Temporário para teste de funções
                User loggedIn = authUseCase.login("admin@safinance.com", "123456");
                return new UserMenu(loggedIn, accountUseCase);
            }

            if (email.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Email e senha não podem ser vazios.");
            }
            User user = authUseCase.login(email, password);
            if (user.isAdmin()) {
                System.out.println("Em desenvolvimento: Menu de Admin ainda não implementado.");
                System.out.println("pressione Enter para tentar novamente.");
                scanner.nextLine(); // Consume the newline character
                return this;
                // return new AdminMenu();
            } else {
                return new UserMenu(user, accountUseCase);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
            System.out.println("Pressione Enter para tentar novamente.");
            scanner.nextLine(); // Consume the newline character
            return this; // Retry login (same state)
        }
        
    }
}
