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
import com.safinance.view.actions.CreateCreditAccountAction;
import com.safinance.view.actions.CreateSavingsAccountAction;
import com.safinance.view.actions.CreateWalletAccountAction;

public class CreateAccountMenu implements BaseMenu {

    private final User user;
    private final User accountOwner;
    private final MenuContext ctx;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public CreateAccountMenu(User user, User accountOwner, MenuContext ctx) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.ctx = ctx;

        registerTransition("1", () -> new CreateWalletAccountAction(accountOwner, ctx.accountUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)), transitions);
        registerTransition("2", () -> new CreateSavingsAccountAction(accountOwner, ctx.accountUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)), transitions);
        registerTransition("3", () -> new CreateCreditAccountAction(accountOwner, ctx.accountUseCase(), () -> new ManageAccountsMenu(user, accountOwner, ctx)), transitions); 
        registerTransition("0", () -> new ManageAccountsMenu(user, accountOwner, ctx), transitions);
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Criar Nova Conta");
        promptService.printMenuOptions("Carteira", "Poupança", "Credito");
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
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }
    
}
