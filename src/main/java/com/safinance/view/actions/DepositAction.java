package com.safinance.view.actions;

import java.util.List;
import java.util.function.Supplier;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

/**
 * Collects the data required to deposit money into one of the user's accounts.
 */
public class DepositAction implements BaseMenu {

    private final User accountOwner;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;
    private final Supplier<BaseMenu> onComplete;

    public DepositAction(User accountOwner, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase, Supplier<BaseMenu> onComplete) {
        this.accountOwner = accountOwner;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
        this.onComplete = onComplete;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Depositar");
    }

    @Override
    public List<String> getOptions() {
        return List.of();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        List<Account> accounts = accountUseCase.listUserAccounts(accountOwner);

        if (accounts.isEmpty()) {
            promptService.printWarning("Você não possui contas para realizar um depósito.");
            promptService.readString("Pressione Enter para voltar ao menu de contas.");
            return onComplete.get();
        }

        printAccounts(promptService, accounts);

        List<String> accountNames = accounts.stream().map(Account::getName).toList();
        String accountName = promptService.readWithOptions("Digite o nome da conta de destino (pressione TAB): ", accountNames).trim();

        Account selectedAccount = findUserAccount(accounts, accountName);

        if (selectedAccount == null) {
            promptService.printError("Conta não encontrada ou não pertence ao usuário.");
            promptService.readString("Pressione Enter para voltar ao menu de contas.");
            return onComplete.get();
        }

        Double amount = promptService.readDouble("Valor do depósito: ");
        if (amount == null || !Double.isFinite(amount) || amount <= 0) {
            promptService.printError("O valor do depósito deve ser um número maior que zero.");
            promptService.readString("Pressione Enter para voltar ao menu de contas.");
            return onComplete.get();
        }

        String description = promptService.readString("Descrição do depósito: ").trim();
        if (description.isBlank()) {
            description = "Deposit";
        }

        try {
            Account updatedAccount = transactionUseCase.deposit(selectedAccount.getId(), amount, description);
            promptService.printSuccess(String.format("Depósito realizado com sucesso. Novo saldo: R$ %.2f", updatedAccount.getBalance()));
        } catch (RuntimeException exception) {
            promptService.printError("Erro ao realizar depósito: " + exception.getMessage());
        }

        promptService.readString("Pressione Enter para voltar ao menu de contas.");
        return onComplete.get();
    }

    private void printAccounts(PromptService promptService, List<Account> accounts) {
        promptService.printInfo("Contas disponíveis:");
        for (Account account : accounts) {
            promptService.printInfo(String.format("%s (%s) | Saldo: R$ %.2f", account.getName(), account.getAccountType(), account.getBalance()));
        }
        promptService.printInfo("");
    }

    private Account findUserAccount(List<Account> accounts, String accountName) {
        return accounts.stream()
                .filter(account -> account.getName().equalsIgnoreCase(accountName))
                .findFirst()
                .orElse(null);
    }
}