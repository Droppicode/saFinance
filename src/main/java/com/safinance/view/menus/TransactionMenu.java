package com.safinance.view.menus;

import com.safinance.core.domain.User;
import com.safinance.view.AbstractMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;
import com.safinance.view.actions.DepositAction;
import com.safinance.view.actions.TransferAction;
import com.safinance.view.actions.WithdrawAction;

/**
 * Menu responsible for routing account transaction operations.
 */
public class TransactionMenu extends AbstractMenu {

    private final User user;
    private final User accountOwner;
    private final MenuContext ctx;

    public TransactionMenu(User user, User accountOwner, MenuContext ctx) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.ctx = ctx;

        registerCommand("1", "Depositar", prompt -> new DepositAction(accountOwner, ctx.accountUseCase(), ctx.transactionUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)));
        registerCommand("2", "Retirar", prompt -> new WithdrawAction(accountOwner, ctx.accountUseCase(), ctx.transactionUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)));
        registerCommand("3", "Transferir", prompt -> new TransferAction(accountOwner, ctx.accountUseCase(), ctx.transactionUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)));
        registerCommand("0", "Voltar", prompt -> new ManageAccountsMenu(user, accountOwner, ctx));
    }

    @Override
    protected void printHeader(PromptService promptService) {
        promptService.printHeader("Operações de Transação");
        promptService.printInfo("Usuário: " + accountOwner.getName());
    }
}