package com.safinance.view.menus;

import com.safinance.core.domain.Role;
import com.safinance.core.domain.User;
import com.safinance.view.AbstractMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;
import com.safinance.view.actions.UpdateOperationTaxAction;
import com.safinance.view.actions.UpdateYieldRateAction;

/**
 * Menu para gerenciar bancos, permitindo que administradores configurem taxas de rendimento e taxas de operação.
 */
public class ManageBanksMenu extends AbstractMenu {

    private final User user;

    public ManageBanksMenu(User user, MenuContext ctx) {
        this.user = user;

        registerCommand("1", "Configurar juros", prompt -> new UpdateYieldRateAction(ctx.bankUseCase(), () -> this));
        registerCommand("2", "Configurar taxas", prompt -> new UpdateOperationTaxAction(ctx.bankUseCase(), () -> this));
        
        registerCommand("0", "Voltar", prompt -> {
            prompt.printSuccess("Retornando ao menu anterior.");
            return new AdminMenu(user, ctx);
        });
    }

    @Override
    protected void printHeader(PromptService promptService) {
        // Verifica se o usuário tem permissão de administrador.
        if (user == null || user.getRole() != Role.ADMIN) {
            promptService.printError("Acesso negado. Apenas administradores podem gerenciar bancos.");
            return;
        }

        promptService.printHeader("Gerenciar Bancos");
        promptService.printInfo("Configurações de cálculo de juros e taxas");
    }   
}
