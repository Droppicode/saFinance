package com.safinance.view.menus;

import java.util.List;

import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.domain.User;
import com.safinance.view.AbstractMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;
import com.safinance.view.actions.ApplyYieldAction;

/**
 * Menu para seleção de contas de poupança, permitindo que o usuário escolha uma conta específica 
 * para aplicar rendimentos.
 */
public class AccountSelectionMenu extends AbstractMenu {
    
    private final User user;    
    private final User accountOwner;
    private final MenuContext ctx;

    public AccountSelectionMenu(User user, User accountOwner, MenuContext ctx) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.ctx = ctx;

        List<SavingsAccount> savingsAccounts = ctx.accountUseCase().listUserAccountsOfType(accountOwner, SavingsAccount.class);
        
        int index = 1;
        for (SavingsAccount sa : savingsAccounts) {
            String label = sa.getName() + " (Saldo: R$ " + sa.getBalance() + ")";
            registerCommand(String.valueOf(index), label, prompt -> new ApplyYieldAction(ctx, sa, () -> new ManageAccountsMenu(user, accountOwner, ctx)));
            index++;
        }

        registerCommand("0", "Voltar", prompt -> new ManageAccountsMenu(user, accountOwner, ctx));
    }

    @Override
    protected void printHeader(PromptService promptService) {
        promptService.printHeader("Selecionar Conta de Poupança");
        promptService.printInfo("");

        List<SavingsAccount> savingsAccounts = ctx.accountUseCase().listUserAccountsOfType(accountOwner, SavingsAccount.class);
        if (savingsAccounts.isEmpty()) {
            promptService.printWarning("Nenhuma conta de poupança encontrada.\n");
        }
    }
}
