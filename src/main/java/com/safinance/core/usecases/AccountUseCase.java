package com.safinance.core.usecases;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.AssetPosition;
import com.safinance.core.domain.Bank;
import com.safinance.core.domain.CreditAccount;
import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.domain.User;
import com.safinance.core.domain.WalletAccount;
import com.safinance.infra.persistence.Repository;


/**
 * Caso de uso para gerenciar contas.
 */
public class AccountUseCase {
    
    // A dependência fica declarada, mas a implementação concreta é desconhecida.
    private final Repository<Account, String> accountRepository;
    private final Bank bank;
    
    /**
     * Construtor da classe.
     * @param accountRepository Repositório para gerenciar contas.
     * @param bank Banco responsável por calcular rendimentos.
     */
    public AccountUseCase(Repository<Account, String> accountRepository, Bank bank) {
        if (accountRepository == null) throw new IllegalArgumentException("O repositório de contas não pode ser nulo.");
        if (bank == null) throw new IllegalArgumentException("O banco não pode ser nulo.");
        this.accountRepository = accountRepository;
        this.bank = bank;
    }
    
    /**
     * Recupera uma conta pelo seu ID.
     * @param id O ID da conta a ser recuperada.
     * @return A conta correspondente ao ID fornecido, ou null se não encontrada.
     */
    public Account getAccount(String id) {
        return accountRepository.findById(id);
    }   
    
    /**
     * Cria uma nova conta de crédito.
     * @param user O usuário proprietário da conta.
     * @param initialBalance O saldo inicial da conta.
     * @param creditLimit O limite de crédito da conta.
     * @return A conta de crédito criada.
     */
    public CreditAccount createCreditAccount(User user, double initialBalance, double creditLimit, String name) {
        checkDuplicateName(user, name);
        CreditAccount acc = new CreditAccount(
            UUID.randomUUID().toString(),
            user.getId(),
            initialBalance,
            creditLimit,
            name
        );
        accountRepository.save(acc);
        return acc;
    }

    /**
     * Cria uma nova conta poupança.
     * @param user O usuário proprietário da conta.
     * @param initialBalance O saldo inicial da conta.
     * @return A conta poupança criada.
     */
    public SavingsAccount createSavingsAccount(User user, double initialBalance, String name) {
        checkDuplicateName(user, name);
        SavingsAccount acc = new SavingsAccount(
            UUID.randomUUID().toString(),
            user.getId(),
            initialBalance,
            name
        );
        accountRepository.save(acc);
        return acc;
    }

    /**
     * Cria uma nova conta carteira.
     * @param user O usuário proprietário da conta.
     * @param initialBalance O saldo inicial da conta.
     * @param portfolio O portfólio de ativos da conta.
     * @return A conta carteira criada.
     */
    public WalletAccount createWalletAccount(User user, double initialBalance, Map<String, AssetPosition> portfolio, String name) {
        checkDuplicateName(user, name);
        WalletAccount acc = new WalletAccount(
            UUID.randomUUID().toString(),
            user.getId(),
            initialBalance,
            portfolio,
            name
        );
        accountRepository.save(acc);
        return acc;
    }

    /**
     * Lista todas as contas de um usuário específico.
     * @param user O usuário cujas contas serão listadas.
     * @return Uma lista de contas pertencentes ao usuário fornecido.
     */
    public List<Account> listUserAccounts(User user) {
        return accountRepository.findAll().stream()
            .filter(account -> account.isOwnedBy(user.getId()))
            .toList();
    }

    /**
     * Verifica se o usuário já possui uma conta com o mesmo nome.
     */
    private void checkDuplicateName(User user, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome da conta não pode ser vazio.");
        }
        boolean exists = listUserAccounts(user).stream()
            .anyMatch(acc -> acc.getName().equalsIgnoreCase(name));
        if (exists) {
            throw new IllegalArgumentException("Você já possui uma conta com o nome '" + name + "'. Escolha outro nome.");
        }
    }

    /**
     * Aplica o rendimento mensal a uma conta poupança.
     * @param account A conta poupança à qual o rendimento será aplicado.
     * @param month O mês para o qual o rendimento será calculado.
     * @return A conta poupança atualizada após a aplicação do rendimento.
     */
    public SavingsAccount applyYield(SavingsAccount account, YearMonth month) {
        SavingsAccount updatedAccount = account.applyMonthlyYield(month, bank);
        accountRepository.save(updatedAccount);
        return updatedAccount;
    }

}
