package com.safinance.view.menus;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.Transaction;
import com.safinance.core.domain.User;
import com.safinance.core.domain.report.AccountDetailedStatement;
import com.safinance.core.domain.report.GlobalBalanceStatement;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Menu responsável por gerar e exibir os relatórios financeiros.
 */
public class ReportMenu implements BaseMenu {

    private final User user;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;
    private final BaseMenu previousMenu;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public ReportMenu(User user, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase, BaseMenu previousMenu) {
        this.user = user;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
        this.previousMenu = previousMenu;

        registerTransition("1", this::generateAccountDetailedStatement, transitions);
        registerTransition("2", this::generateGlobalBalanceStatement, transitions);
        registerTransition("0", () -> previousMenu, transitions);
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Extratos e Relatórios");
        promptService.printMenuOptions(
                "Extrato Detalhado de Conta",
                "Balanço Financeiro Global"
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
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }

    private BaseMenu generateAccountDetailedStatement() {
        PromptService promptService = new PromptService(); // ou injetado via construtor/método se preferir
        List<Account> accounts = accountUseCase.listUserAccounts(user);
        
        if (accounts.isEmpty()) {
            promptService.printWarning("Você não possui contas cadastradas.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        promptService.printInfo("Selecione a conta para o extrato:");
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            System.out.printf("%d - %s [%s] (Saldo: R$ %.2f)%n", i + 1, acc.getClass().getSimpleName(), acc.getId(), acc.getBalance());
        }

        String input = promptService.readString("> Conta: ");
        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < accounts.size()) {
                Account selectedAccount = accounts.get(index);
                List<Transaction> transactions = transactionUseCase.getTransactionsForAccount(selectedAccount.getId());
                
                AccountDetailedStatement statement = new AccountDetailedStatement();
                String report = statement.generateReport(user, List.of(selectedAccount), transactions);
                
                System.out.println(report);
            } else {
                promptService.printError("Conta inválida.");
            }
        } catch (NumberFormatException e) {
            promptService.printError("Entrada inválida.");
        }

        promptService.readString("Pressione Enter para voltar.");
        return this;
    }

    private BaseMenu generateGlobalBalanceStatement() {
        PromptService promptService = new PromptService();
        List<Account> accounts = accountUseCase.listUserAccounts(user);
        
        if (accounts.isEmpty()) {
            promptService.printWarning("Você não possui contas cadastradas para consolidar.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        List<String> accountIds = accounts.stream().map(Account::getId).toList();
        List<Transaction> transactions = transactionUseCase.getTransactionsForAccounts(accountIds);

        GlobalBalanceStatement statement = new GlobalBalanceStatement();
        String report = statement.generateReport(user, accounts, transactions);
        
        System.out.println(report);

        promptService.readString("Pressione Enter para voltar.");
        return this;
    }
}
