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
import com.safinance.core.ports.Repository;
import com.safinance.core.exception.DuplicateAccountException;


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
        return accountRepository.findById(id).orElse(null);
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
     * Lista contas de um usuário filtrando por um tipo específico (Generics),
     * evitando o uso de instanceof e casts na camada de visualização.
     * @param user O usuário cujas contas serão listadas.
     * @param type A classe do tipo da conta desejada.
     * @return Uma lista tipada de contas.
     */
    public <T extends Account> List<T> listUserAccountsOfType(User user, Class<T> type) {
        return listUserAccounts(user).stream()
            .filter(type::isInstance)
            .map(type::cast)
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
            throw new DuplicateAccountException("Você já possui uma conta com o nome '" + name + "'. Escolha outro nome.");
        }
    }



}
