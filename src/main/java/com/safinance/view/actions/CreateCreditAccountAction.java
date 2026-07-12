package com.safinance.view.actions;

import java.util.Collections;
import java.util.List;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.ManageAccountsMenu;
import com.safinance.core.usecases.TransactionUseCase;

public class CreateCreditAccountAction implements BaseMenu {

    private final User user;
    private final User accountOwner;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    public CreateCreditAccountAction(User user, User accountOwner, UserUseCase userUseCase, BankUseCase bankUseCase, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Configuração de Conta de Crédito");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String name = promptService.readString("Nome da Conta de Crédito: ").trim();
        
        double creditLimit = -1;
        String input = promptService.readString("Qual será o limite de crédito da conta? ");
        
        try {
            creditLimit = Double.parseDouble(input.trim().replace(',', '.'));
            if (creditLimit < 0) {
                throw new NumberFormatException("Limite não pode ser negativo.");
            }
        } catch (NumberFormatException e) {
            promptService.printError("Valor de limite inválido.");
            promptService.readString("Pressione Enter para voltar ao menu de contas.");
            return new ManageAccountsMenu(user, accountOwner, userUseCase, bankUseCase, accountUseCase, transactionUseCase);
        }
        
        try {
            accountUseCase.createCreditAccount(accountOwner, 0.0, creditLimit, name);
            promptService.printSuccess("Conta de crédito criada com sucesso no valor de R$ " + creditLimit);
        } catch (Exception e) {
            promptService.printError("Erro ao criar conta de crédito: " + e.getMessage());
        }
        
        promptService.readString("Pressione Enter para voltar ao menu anterior.");
        return new ManageAccountsMenu(user, accountOwner, userUseCase, bankUseCase, accountUseCase, transactionUseCase);
    }
}