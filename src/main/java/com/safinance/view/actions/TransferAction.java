package com.safinance.view.actions;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.TransferType;
import com.safinance.core.domain.User;
import com.safinance.core.exception.InsufficientFundsException;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.InvestmentUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.ManageAccountsMenu;


/**
 * Collects the data required to transfer money between
 * two accounts owned by the authenticated user.
 */
public class TransferAction implements BaseMenu {

    /*
     * Temporary value until Bank becomes responsible for storing
     * and providing transfer tax rates.
     */


    private final User user;
    private final User accountOwner;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;
    private final InvestmentUseCase investmentUseCase;

    private final Map<String, TransferType> transferTypes = Map.of(
            "1", TransferType.PIX,
            "2", TransferType.TED
    );

    public TransferAction(User user, User accountOwner, UserUseCase userUseCase, BankUseCase bankUseCase, AccountUseCase accountUseCase, InvestmentUseCase investmentUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
        this.investmentUseCase = investmentUseCase;
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
        List<Account> accounts = accountUseCase.listUserAccounts(accountOwner);

        if (accounts.size() < 2) {
            promptService.printWarning("Você precisa possuir pelo menos duas contas para realizar uma transferência.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        printAccounts(promptService, accounts);

        List<String> accountNames = accounts.stream().map(Account::getName).toList();
        String sourceAccountName = promptService.readWithOptions("Digite o nome da conta de origem (pressione TAB): ", accountNames).trim();

        Account sourceAccount = findUserAccount(accounts, sourceAccountName);

        if (sourceAccount == null) {
            promptService.printError("Conta de origem não encontrada ou não pertence ao usuário.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        String destinationAccountName = promptService.readWithOptions("Digite o nome da conta de destino (pressione TAB): ", accountNames).trim();

        Account destinationAccount = findUserAccount(accounts, destinationAccountName);

        if (destinationAccount == null) {
            promptService.printError("Conta de destino não encontrada ou não pertence ao usuário.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            promptService.printError("As contas de origem e destino devem ser diferentes.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        double amount;

        try {
            String amountInput = promptService.readString("Valor da transferência: ").trim();

            amount = NumberFormat.getInstance(new Locale("pt", "BR")).parse(amountInput).doubleValue();

            if (!Double.isFinite(amount) || amount <= 0) {
                throw new ParseException("Invalid amount", 0);
            }
        } catch (ParseException exception) {
            promptService.printError("O valor da transferência deve ser um número maior que zero.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        double pixTax = transactionUseCase.previewTransferTax(amount, TransferType.PIX);
        double tedTax = transactionUseCase.previewTransferTax(amount, TransferType.TED);

        promptService.printInfo("");
        promptService.printInfo("Tipos de transferência:");
        promptService.printInfo(String.format("1. PIX — taxa cobrada: R$ %.2f", pixTax));
        promptService.printInfo(String.format("2. TED — taxa cobrada: R$ %.2f", tedTax));

        String transferOption = promptService.readString("Escolha o tipo de transferência: ").trim();

        TransferType transferType = transferTypes.get(transferOption);

        if (transferType == null) {
            promptService.printError("Tipo de transferência inválido.");

            waitForReturn(promptService);
            return backToManageAccounts();
        }

        double tax = transactionUseCase.previewTransferTax(amount, transferType);

        try {
            transactionUseCase.transfer(sourceAccount.getId(), destinationAccount.getId(), amount, transferType);

            Account updatedSource = accountUseCase.getAccount(sourceAccount.getId());

            Account updatedDestination = accountUseCase.getAccount(destinationAccount.getId());

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

    private void waitForReturn(
            PromptService promptService
    ) {
        promptService.readString("Pressione Enter para voltar ao menu de contas.");
    }

    private BaseMenu backToManageAccounts() {
        return new ManageAccountsMenu(user, accountOwner, userUseCase, bankUseCase, accountUseCase, investmentUseCase, transactionUseCase);
    }
}