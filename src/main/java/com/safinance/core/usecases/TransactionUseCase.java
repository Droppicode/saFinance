package com.safinance.core.usecases;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.Transaction;
import com.safinance.core.domain.TransactionFactory;
import com.safinance.core.domain.TransferType;
import com.safinance.core.domain.Bank;
import com.safinance.core.exception.InvalidTransactionException;
import java.util.List;
import com.safinance.infra.persistence.Repository;

/**
 * Coordinates transaction operations between domain objects and repositories.
 */
public class TransactionUseCase {

    private final Repository<Account, String> accountRepository;
    private final Repository<Transaction, String> transactionRepository;
    private final TransactionFactory transactionFactory;
    private final Bank bank;

    public TransactionUseCase(
            Repository<Account, String> accountRepository,
            Repository<Transaction, String> transactionRepository,
            TransactionFactory transactionFactory,
            Bank bank
    ) {
        if (accountRepository == null) {
            throw new IllegalArgumentException("Account repository cannot be null.");
        }

        if (transactionRepository == null) {
            throw new IllegalArgumentException("Transaction repository cannot be null.");
        }

        if (transactionFactory == null) {
            throw new IllegalArgumentException("Transaction factory cannot be null.");
        }

        if (bank == null) {
            throw new IllegalArgumentException("Bank cannot be null.");
        }

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionFactory = transactionFactory;
        this.bank = bank;
    }

    /**
     * Previews the tax for a transfer operation.
     * @param amount amount to transfer
     * @param transferType selected transfer type
     * @return the calculated tax
     */
    public double previewTransferTax(double amount, TransferType transferType) {
        if (transferType == null) {
            throw new InvalidTransactionException("Transfer type cannot be null.");
        }
        return bank.operationCost(amount, transferType.name());
    }

    /**
     * Deposits money into an account.
     *
     * @param accountId account that will receive the money
     * @param amount amount to deposit
     * @param description transaction description
     * @return the updated account
     */
    public Account deposit(String accountId, double amount, String description) {
        Account account = findAccount(accountId);

        Transaction transaction = transactionFactory.createIncome(amount, description, accountId);

        Account updatedAccount = account.process(transaction);

        accountRepository.save(updatedAccount);
        transactionRepository.save(transaction);

        return updatedAccount;
    }

    /**
     * Withdraws money from an account.
     *
     * @param accountId account from which the money will be withdrawn
     * @param amount amount to withdraw
     * @param description transaction description
     * @return the updated account
     */
    public Account withdraw(String accountId, double amount, String description) {
        Account account = findAccount(accountId);

        Transaction transaction = transactionFactory.createExpense(amount, description, accountId);

        Account updatedAccount = account.process(transaction);

        accountRepository.save(updatedAccount);
        transactionRepository.save(transaction);

        return updatedAccount;
    }

    /**
     * Transfers money between two accounts owned by the same user.
     *
     * The source account is charged the transferred amount plus the tax.
     * The destination account receives only the transferred amount.
     *
     * @param sourceAccountId source account identifier
     * @param destinationAccountId destination account identifier
     * @param amount amount to transfer
     * @param transferType selected transfer type
     */
    public void transfer(
            String sourceAccountId,
            String destinationAccountId,
            double amount,
            TransferType transferType
    ) {
        validateAccountId(sourceAccountId, "Source");
        validateAccountId(destinationAccountId, "Destination");

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new InvalidTransactionException("Source and destination accounts must be different.");
        }

        validateAmount(amount);

        if (transferType == null) {
            throw new InvalidTransactionException("Transfer type cannot be null.");
        }

        Account sourceAccount = findAccount(sourceAccountId);
        Account destinationAccount = findAccount(destinationAccountId);

        if (!sourceAccount.isOwnedBySameUserAs(destinationAccount)) {
            throw new InvalidTransactionException("Transfers are only allowed between accounts owned by the same user.");
        }

        double tax = bank.operationCost(amount, transferType.name());

        if (!Double.isFinite(tax) || tax < 0) {
            throw new InvalidTransactionException("Calculated tax must be finite and non-negative.");
        }

        bank.collectFee(tax);

        double totalDebit = amount + tax;

        if (!Double.isFinite(totalDebit)) {
            throw new InvalidTransactionException("Total debit exceeds the supported numeric range.");
        }

        String description =
                "Transfer " + transferType
                        + " from account " + sourceAccountId
                        + " to account " + destinationAccountId;

        Transaction expenseTransaction = transactionFactory.createExpense(totalDebit, description, sourceAccountId, true);

        Transaction incomeTransaction = transactionFactory.createIncome(amount, description, destinationAccountId, true);

        Account updatedSourceAccount = sourceAccount.process(expenseTransaction);

        Account updatedDestinationAccount = destinationAccount.process(incomeTransaction);

        accountRepository.saveAll(List.of(updatedSourceAccount, updatedDestinationAccount));
        transactionRepository.saveAll(List.of(expenseTransaction, incomeTransaction));
    }

    /**
     * Retrieves all transactions associated with a specific account.
     * @param accountId the account identifier
     * @return list of transactions
     */
    public List<Transaction> getTransactionsForAccount(String accountId) {
        validateAccountId(accountId, "Account");
        return transactionRepository.findAll().stream()
                .filter(t -> t.belongsToAccount(accountId))
                .toList();
    }

    /**
     * Retrieves all transactions associated with multiple accounts.
     * @param accountIds list of account identifiers
     * @return list of transactions
     */
    public List<Transaction> getTransactionsForAccounts(List<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return List.of();
        }
        return transactionRepository.findAll().stream()
                .filter(t -> accountIds.contains(t.getAccountId()))
                .toList();
    }

    private Account findAccount(String accountId) {
        validateAccountId(accountId, "Account");

        Account account = accountRepository.findById(accountId);

        if (account == null) {
            throw new InvalidTransactionException("Account not found: " + accountId);
        }

        return account;
    }

    private static void validateAccountId(String accountId, String accountLabel) {
        if (accountId == null || accountId.isBlank()) {
            throw new InvalidTransactionException(accountLabel + " account ID cannot be null or blank.");
        }
    }

    private static void validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new InvalidTransactionException("Transaction amount must be finite and greater than zero.");
        }
    }
}