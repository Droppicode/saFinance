package com.safinance.view.menus;

import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.InvestmentUseCase;
import com.safinance.core.usecases.TransactionUseCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ManageAccountsMenu implements BaseMenu {

    private final User user;
    private final AccountUseCase accountUseCase;
    private final InvestmentUseCase investmentUseCase;
    private final TransactionUseCase transactionUseCase;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public ManageAccountsMenu(User user, AccountUseCase accountUseCase, InvestmentUseCase investmentUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountUseCase = accountUseCase;
        this.investmentUseCase = investmentUseCase;
        this.transactionUseCase = transactionUseCase;

        registerTransition("1", () -> new CreateAccountMenu(this.user, this.accountUseCase, this.investmentUseCase, this.transactionUseCase), transitions);
        registerTransition("2", () -> new TransactionMenu(this.user, this.accountUseCase, this.investmentUseCase, this.transactionUseCase), transitions);
        registerTransition("3", () -> this, transitions);
        registerTransition("0", () -> new UserMenu(user, accountUseCase, investmentUseCase, transactionUseCase), transitions);
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Gerenciar Contas");
        promptService.printInfo("Contas do usuário: " + user.getName());
        promptService.printInfo("");
        
        var accounts = accountUseCase.listUserAccounts(user);
        if (accounts.isEmpty()) {
            promptService.printWarning("Nenhuma conta encontrada para este usuário.");
        } else {
            promptService.printInfo(String.format("%-15s | %-12s | %-10s | %-10s", "Nome", "Tipo", "Saldo", "Limite"));
            promptService.printInfo("---------------------------------------------------------");
            for (var account : accounts) {
                promptService.printInfo(account.getDisplaySummary());
            }
        }

        promptService.printInfo("");
        var wallet = investmentUseCase.getWalletAccount(user);
        if (wallet != null) {
            promptService.printInfo(String.format("Conta carteira existente: saldo R$ %.2f | %d posições", wallet.getBalance(), wallet.getPortfolio().size()));
        } else {
            promptService.printInfo("Nenhuma conta carteira encontrada. Você pode criar uma no menu de contas.");
        }

        promptService.printInfo("");
        promptService.printMenuOptions(
            "Criar nova conta",
            "Depositar / Retirar / Transferir",
            "Aplicar rendimento (para contas poupança)"
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
            if (option.equals("3")) {
                promptService.printWarning("Em desenvolvimento: Funcionalidade ainda não implementada.");
                promptService.readString("Pressione Enter para tentar novamente.");
            }
            return transition.get();
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }
    
}
