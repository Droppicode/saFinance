package com.safinance.view.menus;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.Transaction;
import com.safinance.core.domain.User;
import com.safinance.core.domain.report.AccountDetailedStatement;
import com.safinance.core.domain.report.GlobalBalanceStatement;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Menu responsável por gerar e exibir os relatórios financeiros.
 */
public class ReportMenu implements BaseMenu {

    private final User user;
    private final MenuContext ctx;
    private final BaseMenu previousMenu;

    private final Map<String, Function<PromptService, BaseMenu>> transitions = new HashMap<>();

    public ReportMenu(User user, MenuContext ctx, BaseMenu previousMenu) {
        this.user = user;
        this.ctx = ctx;
        this.previousMenu = previousMenu;
        
        registerTransition("1", this::generateAccountDetailedStatement, transitions);
        registerTransition("2", this::generateGlobalBalanceStatement, transitions);
        registerTransition("0", prompt -> previousMenu, transitions);
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

        Function<PromptService, BaseMenu> transition = transitions.get(option);

        if (transition != null) {
            return transition.apply(promptService);
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }

    private BaseMenu generateAccountDetailedStatement(PromptService promptService) {
        List<Account> accounts = ctx.accountUseCase().listUserAccounts(user);
        
        if (accounts.isEmpty()) {
            promptService.printWarning("Você não possui contas cadastradas.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        promptService.printInfo("Selecione a conta para o extrato:");
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            promptService.printInfo(String.format("%d - %s (%s) (Saldo: R$ %.2f)", i + 1, acc.getName(), acc.getAccountType(), acc.getBalance()));
        }

        String input = promptService.readString("> Conta: ");
        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < accounts.size()) {
                Account selectedAccount = accounts.get(index);
                List<Transaction> transactions = ctx.transactionUseCase().getTransactionsForAccount(selectedAccount.getId());
                
                AccountDetailedStatement statement = new AccountDetailedStatement();
                String report = statement.generateReport(user, List.of(selectedAccount), transactions);
                
                promptService.printInfo(report);
            } else {
                promptService.printError("Conta inválida.");
            }
        } catch (NumberFormatException e) {
            promptService.printError("Entrada inválida.");
        }

        promptService.readString("Pressione Enter para voltar.");
        return this;
    }

    private BaseMenu generateGlobalBalanceStatement(PromptService promptService) {
        List<Account> accounts = ctx.accountUseCase().listUserAccounts(user);
        
        if (accounts.isEmpty()) {
            promptService.printWarning("Você não possui contas cadastradas para consolidar.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        List<String> accountIds = accounts.stream().map(Account::getId).toList();
        List<Transaction> transactions = ctx.transactionUseCase().getTransactionsForAccounts(accountIds);

        GlobalBalanceStatement statement = new GlobalBalanceStatement();
        String report = statement.generateReport(user, accounts, transactions);
        
        promptService.printInfo(report);

        promptService.readString("Pressione Enter para voltar.");
        return this;
    }
}
