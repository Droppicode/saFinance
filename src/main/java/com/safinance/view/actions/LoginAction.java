package com.safinance.view.actions;

import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.WelcomeMenu;
import com.safinance.view.menus.UserMenu;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.InvestmentUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.core.usecases.TransactionUseCase;

import java.util.List;
import java.util.Collections;

/**
 * Menu de login para a aplicação.
 */
public class LoginAction implements BaseMenu {

    private final AuthUseCase authUseCase;
    private final UserUseCase userUseCase;
    private final AccountUseCase accountUseCase;
    private final InvestmentUseCase investmentUseCase;
    private final TransactionUseCase transactionUseCase;

    // Construtor da classe.
    public LoginAction(AuthUseCase authUseCase, UserUseCase userUseCase, AccountUseCase accountUseCase, InvestmentUseCase investmentUseCase, TransactionUseCase transactionUseCase) {
        this.authUseCase = authUseCase;
        this.userUseCase = userUseCase;
        this.accountUseCase = accountUseCase;
        this.investmentUseCase = investmentUseCase;
        this.transactionUseCase = transactionUseCase;
    }

    /**
     * Exibe o menu de login e retorna o próximo estado.
     */
    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Fazer Login (Use email 'teste' para logar com admin)");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList(); // Sem autocomplete para email/senha
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String email = promptService.readString("Email: ");
        String password = promptService.readString("Senha: ");

        try { 
            if (email.equals("teste")) { // Temporário para teste de funções
                User loggedIn = authUseCase.login("admin@safinance.com", "123456");
                return new UserMenu(loggedIn, accountUseCase, investmentUseCase, transactionUseCase);
            }

            if (email.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Email e senha não podem ser vazios.");
            }
            User user = authUseCase.login(email, password);
            if (user.isAdmin()) {
                promptService.printWarning("Em desenvolvimento: Menu de Admin ainda não implementado.");
                promptService.readString("pressione Enter para tentar novamente.");
                return new WelcomeMenu(authUseCase, userUseCase, accountUseCase, investmentUseCase, transactionUseCase);
            } else {
                return new UserMenu(user, accountUseCase, investmentUseCase, transactionUseCase);
            }
        } catch (IllegalArgumentException e) {
            promptService.printError("Erro: " + e.getMessage());
            promptService.readString("Pressione Enter para tentar novamente.");
            return new WelcomeMenu(authUseCase, userUseCase, accountUseCase, investmentUseCase, transactionUseCase); // Volta ao início se errar
        }
    }
}