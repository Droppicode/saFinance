package com.safinance.view.menus;

import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.actions.CreateCreditAccountAction;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.TransactionUseCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.safinance.view.actions.CreateWalletAccountAction;
import com.safinance.view.actions.CreateSavingsAccountAction;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.actions.CreateCreditAccountAction;

public class CreateAccountMenu implements BaseMenu {

    private final User user;
    private final User accountOwner;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public CreateAccountMenu(User user, User accountOwner, UserUseCase userUseCase, BankUseCase bankUseCase, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;

        registerTransition("1", () -> new CreateWalletAccountAction(user, accountUseCase, transactionUseCase), transitions);
        registerTransition("2", () -> new CreateSavingsAccountAction(user, accountUseCase, transactionUseCase), transitions);
        registerTransition("3", () -> new CreateCreditAccountAction(user, accountUseCase, transactionUseCase), transitions); 
        registerTransition("0", () -> new ManageAccountsMenu(user, accountUseCase, transactionUseCase), transitions);
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Criar Nova Conta");
        promptService.printMenuOptions("Corrente", "Poupança", "Credito");
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
