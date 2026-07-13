package com.safinance.core.domain;

import com.safinance.core.exception.InsufficientFundsException;
import com.safinance.core.exception.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class WalletAccountTest {

    private WalletAccount wallet;
    private TransactionFactory factory;

    @BeforeEach
    void setUp() {
        wallet = new WalletAccount("acc-1", "user-1", 1000.0, new HashMap<>(), "Minha Carteira");
        factory = new TransactionFactory();
    }

    @Test
    void testProcessIncomeTransaction_ShouldReturnNewAccountWithUpdatedBalance() {
        Transaction income = factory.createIncome(500.0, "Depósito", "acc-1");
        
        Account updatedWallet = wallet.process(income);

        // Verifica o padrão Wither (imutabilidade)
        assertNotSame(wallet, updatedWallet, "O método process deve retornar uma NOVA instância");
        assertEquals(1000.0, wallet.getBalance(), "A conta original não deve ser alterada");
        assertEquals(1500.0, updatedWallet.getBalance(), "A nova conta deve ter o saldo atualizado");
    }

    @Test
    void testProcessExpenseTransaction_ShouldReturnNewAccountWithDecreasedBalance() {
        Transaction expense = factory.createExpense(300.0, "Saque", "acc-1");
        
        Account updatedWallet = wallet.process(expense);

        assertEquals(700.0, updatedWallet.getBalance());
        assertEquals(1000.0, wallet.getBalance());
    }

    @Test
    void testProcessExpenseTransaction_WithoutBalance_ShouldThrowInsufficientFundsException() {
        Transaction expense = factory.createExpense(1500.0, "Saque Maior que Saldo", "acc-1");
        
        // Verifica o Fail-Fast e a estratégia de exceções semânticas
        assertThrows(InsufficientFundsException.class, () -> {
            wallet.process(expense);
        }, "Deve falhar imediatamente ao tentar sacar mais do que o saldo disponível");
    }

    @Test
    void testProcessTransaction_FromAnotherAccount_ShouldThrowInvalidTransactionException() {
        Transaction invalidTx = factory.createIncome(100.0, "Tx de outra conta", "acc-999");
        
        assertThrows(InvalidTransactionException.class, () -> {
            wallet.process(invalidTx);
        }, "A conta deve validar se a transação pertence a ela mesma");
    }

    @Test
    void testProcessBuyAssetTransaction_ShouldUpdatePortfolio() {
        Stock petr4 = new Stock("ast-1", "PETR4", "Ações Petrobras", "Petrobras", 0.05);
        Transaction buyTx = factory.createBuyAsset(petr4, 10, 35.0, "acc-1"); // 350.0 total
        
        WalletAccount updatedWallet = (WalletAccount) wallet.process(buyTx);
        
        assertEquals(650.0, updatedWallet.getBalance(), "O saldo deve reduzir em 350");
        assertEquals(1, updatedWallet.getPortfolio().size(), "Deve haver 1 ativo no portfólio");
        
        AssetPosition pos = updatedWallet.getPortfolio().get("PETR4");
        assertNotNull(pos);
        assertEquals(10, pos.getQuantity());
        assertEquals(35.0, pos.getAveragePrice());
    }
}
