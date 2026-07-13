package com.safinance.view.menus;

import com.safinance.core.domain.Role;
import com.safinance.core.domain.User;
import com.safinance.view.AbstractMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;
import com.safinance.view.actions.RegisterAction;

/**
 * Menu para gerenciar usuários do sistema.
 */
public class ManageUsersMenu extends AbstractMenu {

    private final User user;
    private final MenuContext ctx;

    public ManageUsersMenu(User user, MenuContext ctx) {
        this.user = user;
        this.ctx = ctx;

        registerCommand("1", "Criar Novo Usuário", prompt -> new RegisterAction(user, ctx.userUseCase(), () -> this));
        registerCommand("2", "Gerenciar Contas do Usuário", prompt -> new UserSelectionMenu(user, ctx));
        registerCommand("0", "Voltar", prompt -> new AdminMenu(user, ctx));
    }

    @Override
    protected void printHeader(PromptService promptService) {
        // Verifica se o usuário é um administrador
        if (user == null || user.getRole() != Role.ADMIN) {
            promptService.printError("Acesso negado. Apenas administradores podem gerenciar usuários.");
            return;
        }

        promptService.printHeader("Gerenciar Usuários");
        promptService.printInfo("Usuários disponíveis:\n");

        var users = ctx.userUseCase().getAllUsers();
        if (users.isEmpty()) {
            promptService.printWarning("Nenhum usuário encontrado.");
        } else {
            promptService.printInfo(String.format("%-20s | %-30s", "Nome", "Email"));
            promptService.printInfo("--------------------------------------------------");
            for (var u : users) {
                promptService.printInfo(String.format("%-20s | %-30s", u.getName(), u.getEmail()));
            }
        }
        promptService.printInfo("");
    }
}
