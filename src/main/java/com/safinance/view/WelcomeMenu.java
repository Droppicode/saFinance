package com.safinance.view;

import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.UserUseCase;

import java.util.Arrays;
import java.util.List;

/**
 * Menu inicial (Boas-vindas) da aplicação.
 * Responsável por rotear para o Login ou Registro.
 */
public class WelcomeMenu implements BaseMenu {

    private final AuthUseCase authUseCase;
    private final UserUseCase userUseCase;
    private final AccountUseCase accountUseCase;

    public WelcomeMenu(AuthUseCase authUseCase, UserUseCase userUseCase, AccountUseCase accountUseCase) {
        this.authUseCase = authUseCase;
        this.userUseCase = userUseCase;
        this.accountUseCase = accountUseCase;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Bem-vindo ao ObjectFinance");
        promptService.printMenuOptions(
            "Fazer Login",
            "Criar Nova Conta"
        );
    }

    @Override
    public List<String> getOptions() {
        return Arrays.asList("1", "2", "0");
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        int option = promptService.readIntOption("> Escolha uma opção: ");

        switch (option) {
            case 1:
                return new LoginMenu(authUseCase, userUseCase, accountUseCase);
            case 2:
                return new RegisterMenu(authUseCase, userUseCase, accountUseCase);
            case 0:
                promptService.printSuccess("Saindo do sistema. Até logo!");
                return null;
            default:
                promptService.printError("Opção inválida.");
                promptService.readString("Pressione Enter para tentar novamente.");
                return this;
        }
    }
}
