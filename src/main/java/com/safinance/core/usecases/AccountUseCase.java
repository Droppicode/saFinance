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
    public CreditAccount createCreditAccount(User user, double initialBalance, double creditLimit) {
        CreditAccount acc = new CreditAccount(
            UUID.randomUUID().toString(),
            user.getId(),
            initialBalance,
            creditLimit
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
    public SavingsAccount createSavingsAccount(User user, double initialBalance) {
        SavingsAccount acc = new SavingsAccount(
            UUID.randomUUID().toString(),
            user.getId(),
            initialBalance
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
    public WalletAccount createWalletAccount(User user, double initialBalance, Map<String, AssetPosition> portfolio) {
        WalletAccount acc = new WalletAccount(
            UUID.randomUUID().toString(),
            user.getId(),
            initialBalance,
            portfolio
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
