package com.safinance.core.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionFactoryTest {

    private TransactionFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TransactionFactory();
    }

    @Test
    void testCreateIncome_ShouldGenerateValidTransaction() {
        Transaction income = factory.createIncome(500.0, "Salário", "acc-1");
        
        assertNotNull(income.getId());
        assertEquals(500.0, income.getAmount());
        assertEquals("Salário", income.getDescription());
        assertEquals("acc-1", income.getAccountId());
        assertTrue(income.isIncome());
        assertFalse(income.isTransfer());
        assertNotNull(income.getDate());
    }

    @Test
    void testCreateExpense_ShouldGenerateNegativeAmount() {
        Transaction expense = factory.createExpense(200.0, "Conta de Luz", "acc-1");
        
        assertFalse(expense.isIncome());
        assertNotNull(expense.getId());
    }
}
