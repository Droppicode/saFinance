package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.core.domain.User;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;
import com.safinance.view.actions.DepositAction;
import com.safinance.view.actions.TransferAction;
import com.safinance.view.actions.WithdrawAction;

/**
 * Menu responsible for routing account transaction operations.
 */
public class TransactionMenu implements BaseMenu {

    private final User user;
    private final User accountOwner;
    private final MenuContext ctx;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public TransactionMenu(User user, User accountOwner, MenuContext ctx) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.ctx = ctx;

        registerTransition("1", () -> new DepositAction(accountOwner, ctx.accountUseCase(), ctx.transactionUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)), transitions);
        registerTransition("2", () -> new WithdrawAction(accountOwner, ctx.accountUseCase(), ctx.transactionUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)), transitions);
        registerTransition("3", () -> new TransferAction(accountOwner, ctx.accountUseCase(), ctx.transactionUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)), transitions);
        registerTransition("0", () -> new ManageAccountsMenu(user, accountOwner, ctx), transitions);
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Operações de Transação");
        promptService.printInfo("Usuário: " + accountOwner.getName());

        promptService.printMenuOptions(
                "Depositar",
                "Retirar",
                "Transferir"
        );
    }

    @Override
    public List<String> getOptions() {
        return new ArrayList<>(transitions.keySet());
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha uma opção: ").trim();

        Supplier<BaseMenu> transition = transitions.get(option);

        if (transition != null) {
            return transition.get();
        }

        promptService.printError("Opção inválida.");
        promptService.readString("Pressione Enter para tentar novamente.");

        return this;
    }
}