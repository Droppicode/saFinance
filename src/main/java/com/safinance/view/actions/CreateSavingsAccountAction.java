package com.safinance.view.actions;

import java.util.Collections;
import java.util.List;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.ManageAccountsMenu;

public class CreateSavingsAccountAction implements BaseMenu {

    private final User user;
    private final User accountOwner;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    public CreateSavingsAccountAction(User user, User accountOwner, UserUseCase userUseCase, BankUseCase bankUseCase, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Criar Conta Poupança");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String name = promptService.readString("Nome da Conta Poupança: ").trim();
        
        try {
            accountUseCase.createSavingsAccount(accountOwner, 0.0, name);
            promptService.printSuccess("Conta poupança '" + name + "' criada com sucesso!");
        } catch (Exception e) {
            promptService.printError("Erro ao criar conta poupança: " + e.getMessage());
        }
        
        promptService.readString("Pressione Enter para voltar ao menu de contas.");
        return new ManageAccountsMenu(user, accountOwner, userUseCase, bankUseCase, accountUseCase, transactionUseCase);
    }
}
