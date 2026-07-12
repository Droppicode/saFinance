package com.safinance.view.actions;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.User;
import com.safinance.core.exception.InsufficientFundsException;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.ManageAccountsMenu;

import java.util.Collections;
import java.util.List;

/**
 * Collects the data required to withdraw money from one of the user's accounts.
 */
public class WithdrawAction implements BaseMenu {

    private final User user;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    public WithdrawAction(User user, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Retirar");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        List<Account> accounts = accountUseCase.listUserAccounts(user);

        if (accounts.isEmpty()) {
            promptService.printWarning("Você não possui contas para realizar uma retirada.");

            promptService.readString("Pressione Enter para voltar ao menu de contas.");

            return backToManageAccounts();
        }

        printAccounts(promptService, accounts);

        String accountId = promptService.readString("Digite o ID da conta de origem: ").trim();

        Account selectedAccount = findUserAccount(accounts, accountId);

        if (selectedAccount == null) {
            promptService.printError("Conta não encontrada ou não pertence ao usuário.");

            promptService.readString("Pressione Enter para voltar ao menu de contas.");

            return backToManageAccounts();
        }

        double amount;

        try {
            String amountInput = promptService.readString("Valor da retirada: ").trim();

            amount = Double.parseDouble(amountInput.trim().replace(',', '.'));

            if (!Double.isFinite(amount) || amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            promptService.printError("O valor da retirada deve ser um número maior que zero.");

            promptService.readString("Pressione Enter para voltar ao menu de contas.");

            return backToManageAccounts();
        }

        String description = promptService.readString("Descrição da retirada: ").trim();

        if (description.isBlank()) {
            description = "Withdrawal";
        }

        try {
            Account updatedAccount = transactionUseCase.withdraw(selectedAccount.getId(), amount,description);

            promptService.printSuccess(String.format("Retirada realizada com sucesso. Novo saldo: R$ %.2f",updatedAccount.getBalance()));

        } catch (InsufficientFundsException exception) {
            promptService.printError("Saldo ou limite insuficiente: " + exception.getMessage()
            );

        } catch (RuntimeException exception) {
            promptService.printError("Erro ao realizar retirada: " + exception.getMessage()
            );
        }

        promptService.readString("Pressione Enter para voltar ao menu de contas.");

        return backToManageAccounts();
    }

    private void printAccounts(PromptService promptService, List<Account> accounts) {
        promptService.printInfo("Contas disponíveis:");

        for (Account account : accounts) {
            promptService.printInfo(String.format("ID: %s | Saldo: R$ %.2f", account.getId(), account.getBalance()));
        }

        promptService.printInfo("");
    }

    private Account findUserAccount(List<Account> accounts, String accountId) {
        return accounts.stream()
                .filter(account -> account.getId().equals(accountId))
                .findFirst()
                .orElse(null);
    }

    private BaseMenu backToManageAccounts() {
        return new ManageAccountsMenu(user, accountUseCase, transactionUseCase);
    }
}