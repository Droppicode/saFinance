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

public class CreateAccountMenu implements BaseMenu {

    private final User user;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public CreateAccountMenu(User user, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;

        registerTransition("1", () -> {
            accountUseCase.createWalletAccount(user, 0.0, null); 
            return new ManageAccountsMenu(user, accountUseCase, transactionUseCase);
        }, transitions);

        registerTransition("2", () -> {
            accountUseCase.createSavingsAccount(user, 0.0);
            return new ManageAccountsMenu(user, accountUseCase, transactionUseCase);
        }, transitions);

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
            if (option.equals("1")) {
                promptService.printSuccess("Conta corrente criada com sucesso!");
                promptService.readString("Pressione Enter para voltar ao menu anterior.");
            } else if (option.equals("2")) {
                promptService.printSuccess("Conta poupança criada com sucesso!");
                promptService.readString("Pressione Enter para voltar ao menu anterior.");
            }
            return transition.get();
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }
    
}
