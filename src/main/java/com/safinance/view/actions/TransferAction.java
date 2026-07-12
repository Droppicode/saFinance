package com.safinance.view.actions;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.TransferType;
import com.safinance.core.domain.User;
import com.safinance.core.domain.tax.ExemptTax;
import com.safinance.core.domain.tax.StandardTax;
import com.safinance.core.domain.tax.TaxStrategy;
import com.safinance.core.exception.InsufficientFundsException;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.ManageAccountsMenu;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Collects the data required to transfer money between
 * two accounts owned by the authenticated user.
 */
public class TransferAction implements BaseMenu {

    /*
     * Temporary value until Bank becomes responsible for storing
     * and providing transfer tax rates.
     */
    private static final double TEMPORARY_TED_RATE = 0.02;

    private final User user;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    private final Map<String, TransferType> transferTypes = Map.of(
            "1", TransferType.PIX,
            "2", TransferType.TED
    );

    private final Map<TransferType, TaxStrategy> taxStrategies = Map.of(
            TransferType.PIX, new ExemptTax(),
            TransferType.TED, new StandardTax(TEMPORARY_TED_RATE)
    );

    public TransferAction(User user, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Transferir entre Contas");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        List<Account> accounts = accountUseCase.listUserAccounts(user);

        if (accounts.size() < 2) {
            promptService.printWarning("Você precisa possuir pelo menos duas contas para realizar uma transferência.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        printAccounts(promptService, accounts);

        String sourceAccountId = promptService.readString("Digite o ID da conta de origem: ").trim();

        Account sourceAccount = findUserAccount(accounts, sourceAccountId);

        if (sourceAccount == null) {
            promptService.printError("Conta de origem não encontrada ou não pertence ao usuário.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        String destinationAccountId = promptService.readString("Digite o ID da conta de destino: ").trim();

        Account destinationAccount = findUserAccount(accounts, destinationAccountId);

        if (destinationAccount == null) {
            promptService.printError("Conta de destino não encontrada ou não pertence ao usuário.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        if (sourceAccountId.equals(destinationAccountId)) {
            promptService.printError("As contas de origem e destino devem ser diferentes.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        double amount;

        try {
            String amountInput = promptService.readString("Valor da transferência: ").trim();

            amount = Double.parseDouble(amountInput.trim().replace(',', '.'));

            if (!Double.isFinite(amount) || amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            promptService.printError("O valor da transferência deve ser um número maior que zero.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        promptService.printInfo("");
        promptService.printInfo("Tipos de transferência:");
        promptService.printInfo("1. PIX — sem taxa");
        promptService.printInfo(String.format("2. TED — taxa temporária de %.2f%%", TEMPORARY_TED_RATE * 100));

        String transferOption = promptService.readString("Escolha o tipo de transferência: ").trim();

        TransferType transferType = transferTypes.get(transferOption);

        if (transferType == null) {
            promptService.printError("Tipo de transferência inválido.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        TaxStrategy taxStrategy = taxStrategies.get(transferType);

        double tax = taxStrategy.calculateTax(amount);

        try {
            transactionUseCase.transfer(sourceAccountId, destinationAccountId, amount,transferType, taxStrategy);

            Account updatedSource = accountUseCase.getAccount(sourceAccountId);

            Account updatedDestination = accountUseCase.getAccount(destinationAccountId);

            promptService.printSuccess("Transferência realizada com sucesso.");

            promptService.printInfo(String.format("Valor transferido: R$ %.2f", amount));

            promptService.printInfo(String.format("Taxa cobrada: R$ %.2f", tax));

            promptService.printInfo(String.format("Novo saldo da origem: R$ %.2f", updatedSource.getBalance()));

            promptService.printInfo(String.format("Novo saldo do destino: R$ %.2f", updatedDestination.getBalance()));

        } catch (InsufficientFundsException exception) {
            promptService.printError("Saldo ou limite insuficiente: " + exception.getMessage());

        } catch (RuntimeException exception) {
            promptService.printError("Erro ao realizar transferência: " + exception.getMessage());
        }

        waitForReturn(promptService);
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

    private void waitForReturn(
            PromptService promptService
    ) {
        promptService.readString("Pressione Enter para voltar ao menu de contas.");
    }

    private BaseMenu backToManageAccounts() {
        return new ManageAccountsMenu(user, accountUseCase, transactionUseCase);
    }
}