package com.safinance.view.menus;

import com.safinance.core.domain.User;
import com.safinance.view.AbstractMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;
import com.safinance.view.actions.CreateCreditAccountAction;
import com.safinance.view.actions.CreateSavingsAccountAction;
import com.safinance.view.actions.CreateWalletAccountAction;

public class CreateAccountMenu extends AbstractMenu {

    private final User user;
    private final User accountOwner;
    private final MenuContext ctx;

    public CreateAccountMenu(User user, User accountOwner, MenuContext ctx) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.ctx = ctx;

        registerCommand("1", "Carteira", prompt -> new CreateWalletAccountAction(accountOwner, ctx.accountUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)));
        registerCommand("2", "Poupança", prompt -> new CreateSavingsAccountAction(accountOwner, ctx.accountUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)));
        registerCommand("3", "Credito", prompt -> new CreateCreditAccountAction(accountOwner, ctx.accountUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx))); 
        registerCommand("0", "Voltar", prompt -> new ManageAccountsMenu(user, accountOwner, ctx));
    }

    @Override
    protected void printHeader(PromptService promptService) {
        promptService.printHeader("Criar Nova Conta");
    }
}
