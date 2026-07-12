package com.safinance.view.actions;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.InvestmentUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.ManageAccountsMenu;

import java.util.Collections;
import java.util.List;

public class CreateSavingsAccountAction implements BaseMenu {

    private final User user;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;
    private final InvestmentUseCase investmentUseCase;

    public CreateSavingsAccountAction(User user, AccountUseCase accountUseCase, InvestmentUseCase investmentUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
        this.investmentUseCase = investmentUseCase;
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
            accountUseCase.createSavingsAccount(user, 0.0, name);
            promptService.printSuccess("Conta poupança '" + name + "' criada com sucesso!");
        } catch (Exception e) {
            promptService.printError("Erro ao criar conta poupança: " + e.getMessage());
        }
        
        promptService.readString("Pressione Enter para voltar ao menu de contas.");
        return new ManageAccountsMenu(user, accountUseCase, investmentUseCase, transactionUseCase);
    }
}
