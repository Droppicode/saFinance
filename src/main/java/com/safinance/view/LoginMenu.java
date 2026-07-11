package com.safinance.view;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.UserUseCase;

import java.util.List;
import java.util.Collections;

/**
 * Menu de login para a aplicação.
 */
public class LoginMenu implements BaseMenu {

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
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Login");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList(); // Sem autocomplete para email/senha
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String email = promptService.readString("Email: ");
        String password = promptService.readString("Senha: ");

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
                promptService.printWarning("Em desenvolvimento: Menu de Admin ainda não implementado.");
                promptService.readString("pressione Enter para tentar novamente.");
                return this;
            } else {
                return new UserMenu(user, accountUseCase);
            }
        } catch (IllegalArgumentException e) {
            promptService.printError("Erro: " + e.getMessage());
            promptService.readString("Pressione Enter para tentar novamente.");
            return this; // Retry login (same state)
        }
    }
}
