package com.safinance.view.menus;

import com.safinance.core.domain.User;
import com.safinance.view.AbstractMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;

/**
 * Menu principal para usuários administradores.
 */
public class AdminMenu extends AbstractMenu {

    private final User user;

    public AdminMenu(User user, MenuContext ctx) {
        this.user = user;

        registerCommand("1", "Gerenciar usuários", prompt -> new ManageUsersMenu(user, ctx));
        registerCommand("2", "Gerenciar contas", prompt -> new ManageAccountsMenu(user, user, ctx));
        registerCommand("3", "Gerenciar bancos", prompt -> new ManageBanksMenu(user, ctx));
        
        registerCommand("0", "Voltar", prompt -> {
            prompt.printSuccess("Voltando ao menu do usuário...");
            return new UserMenu(user, ctx);
        });
    }

    @Override
    protected void printHeader(PromptService promptService) {
        promptService.printHeader("Menu do Administrador");
        promptService.printInfo("Bem-vindo, " + user.getName() + "!");
    }
}
