package com.safinance.view.menus;

import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.actions.LoginAction;
import com.safinance.view.actions.RegisterAction;

import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.UserUseCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Menu inicial (Boas-vindas) da aplicação.
 * Responsável por rotear para o Login ou Registro.
 */
public class WelcomeMenu implements BaseMenu {

    private final AuthUseCase authUseCase;
    private final UserUseCase userUseCase;
    private final AccountUseCase accountUseCase;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public WelcomeMenu(AuthUseCase authUseCase, UserUseCase userUseCase, AccountUseCase accountUseCase) {
        this.authUseCase = authUseCase;
        this.userUseCase = userUseCase;
        this.accountUseCase = accountUseCase;

        // Registro Dinâmico de Rotas (Lab 4 State Pattern)
        registerTransition("1", () -> new LoginAction(authUseCase, userUseCase, accountUseCase), transitions);
        registerTransition("2", () -> new RegisterAction(authUseCase, userUseCase, accountUseCase), transitions);
        registerTransition("0", () -> null, transitions);
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
        return new ArrayList<>(transitions.keySet()); // Auto-atualizável com as chaves cadastradas
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha uma opção: ").trim();
        Supplier<BaseMenu> transition = transitions.get(option);

        if (transition != null) {
            if (option.equals("0")) {
                promptService.printSuccess("Saindo do sistema. Até logo!");
            }
            return transition.get();
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }
}
