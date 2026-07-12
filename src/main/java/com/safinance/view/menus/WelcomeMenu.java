package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;
import com.safinance.view.actions.LoginAction;
import com.safinance.view.actions.RegisterAction;
import com.safinance.view.menus.AdminMenu;
import com.safinance.view.menus.UserMenu;
/**
 * Menu inicial (Boas-vindas) da aplicação.
 * Responsável por rotear para o Login ou Registro.
 */
public class WelcomeMenu implements BaseMenu {

    private final MenuContext ctx;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public WelcomeMenu(MenuContext ctx) {
        this.ctx = ctx;

        // Registro Dinâmico de Rotas (Lab 4 State Pattern)
        registerTransition("1", () -> new LoginAction(
            ctx.authUseCase(),
            loggedIn -> {
                switch (loggedIn.getRole()) {
                    case ADMIN: return new AdminMenu(loggedIn, ctx);
                    case REGULAR: return new UserMenu(loggedIn, ctx);
                    default: return new AdminMenu(loggedIn, ctx);
                }
            },
            () -> this
        ), transitions);

        registerTransition("2", () -> new RegisterAction(null, ctx.userUseCase(), () -> this), transitions);
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
