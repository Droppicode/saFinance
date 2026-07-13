package com.safinance.view.menus;

import com.safinance.view.AbstractMenu;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;
import com.safinance.view.actions.LoginAction;
import com.safinance.view.actions.RegisterAction;

/**
 * Menu inicial (Boas-vindas) da aplicação.
 * Responsável por rotear para o Login ou Registro.
 */
public class WelcomeMenu extends AbstractMenu {

    private final MenuContext ctx;

    public WelcomeMenu(MenuContext ctx) {
        this.ctx = ctx;

        registerCommand("1", "Fazer Login", prompt -> new LoginAction(
            ctx.authUseCase(),
            loggedIn -> {
                ctx.transactionUseCase().catchUpSavingsYields(loggedIn);
                return new UserMenu(loggedIn, ctx);
            },
            () -> this
        ));

        registerCommand("2", "Criar Nova Conta", prompt -> new RegisterAction(null, ctx.userUseCase(), () -> this));
        
        registerCommand("0", "Sair", prompt -> {
            prompt.printSuccess("Saindo do sistema. Até logo!");
            return null;
        });
    }

    @Override
    protected void printHeader(PromptService promptService) {
        promptService.printHeader("Bem-vindo ao ObjectFinance");
    }
}
