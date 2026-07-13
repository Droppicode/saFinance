package com.safinance.view.menus;

import com.safinance.core.domain.User;
import com.safinance.view.AbstractMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;

/**
 * Menu para seleção de usuários, permitindo que o administrador escolha um usuário específico para gerenciar suas contas.
 */
public class UserSelectionMenu extends AbstractMenu {

    private final User user;
    private final MenuContext ctx;

    public UserSelectionMenu(User user, MenuContext ctx) {
        this.user = user;
        this.ctx = ctx;

        var users = ctx.userUseCase().getAllUsers();
        
        int index = 1;
        for (User u : users) {
            String label = u.getName() + " (" + u.getEmail() + ")";
            registerCommand(String.valueOf(index), label, prompt -> new ManageAccountsMenu(user, u, ctx));
            index++;
        }

        registerCommand("0", "Voltar", prompt -> new ManageUsersMenu(user, ctx));
    }

    @Override
    protected void printHeader(PromptService promptService) {
        promptService.printHeader("Seleção de Usuário");
        promptService.printInfo("");

        var users = ctx.userUseCase().getAllUsers();
        if (users.isEmpty()) {
            promptService.printWarning("Nenhum usuário encontrado.\n");
        }
    }
}
