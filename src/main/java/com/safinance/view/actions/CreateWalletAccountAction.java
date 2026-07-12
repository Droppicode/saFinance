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

public class CreateWalletAccountAction implements BaseMenu {

    private final User user;
    private final User accountOwner;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    public CreateWalletAccountAction(User user, User accountOwner, UserUseCase userUseCase, BankUseCase bankUseCase, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Criar Conta Corrente / Carteira");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String name = promptService.readString("Nome da Conta Corrente: ").trim();
        
        try {
            accountUseCase.createWalletAccount(accountOwner, 0.0, null, name);
            promptService.printSuccess("Conta corrente '" + name + "' criada com sucesso!");
        } catch (Exception e) {
            promptService.printError("Erro ao criar conta corrente: " + e.getMessage());
        }
        
        promptService.readString("Pressione Enter para voltar ao menu de contas.");
        return new ManageAccountsMenu(user, accountOwner, userUseCase, bankUseCase, accountUseCase, transactionUseCase);
    }
}
