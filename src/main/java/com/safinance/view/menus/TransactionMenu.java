package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
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
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public TransactionMenu(User user, User accountOwner, AccountUseCase accountUseCase, UserUseCase userUseCase,  BankUseCase bankUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;

        registerTransition("1", () -> new DepositAction(user, accountOwner, userUseCase, bankUseCase, accountUseCase, transactionUseCase), transitions);

        registerTransition("2", () -> new WithdrawAction(user, accountOwner, userUseCase, bankUseCase, accountUseCase, transactionUseCase), transitions);

        registerTransition("3", () -> new TransferAction(user, accountOwner, userUseCase, bankUseCase, accountUseCase, transactionUseCase), transitions);

        registerTransition("0", () -> new ManageAccountsMenu(user, accountOwner, userUseCase, bankUseCase, accountUseCase, transactionUseCase), transitions);
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