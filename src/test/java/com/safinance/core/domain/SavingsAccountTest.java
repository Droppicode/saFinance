package com.safinance.core.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class SavingsAccountTest {

    private SavingsAccount savings;
    private Bank bank;

    @BeforeEach
    void setUp() {
        YearMonth inception = YearMonth.of(2023, 1);
        savings = new SavingsAccount("acc-2", "user-1", 1000.0, "Minha Poupança", inception);
        bank = new Bank(inception, 0.01); // Taxa de 1%
    }

    @Test
    void testCalculateYieldAmount_ShouldApplyBankRate() {
        double yield = savings.calculateYieldAmount(YearMonth.of(2023, 1), bank);
        
        assertEquals(10.0, yield, 0.01, "O rendimento de 1% sobre 1000 deve ser 10");
    }

    @Test
    void testWithLastYieldMonth_ShouldReturnNewInstance() {
        YearMonth newMonth = YearMonth.of(2023, 2);
        SavingsAccount updatedSavings = savings.withLastYieldMonth(newMonth);
        
        assertNotSame(savings, updatedSavings, "O padrão Wither deve garantir imutabilidade");
        assertEquals(newMonth, updatedSavings.getLastYieldMonth());
        assertEquals(1000.0, updatedSavings.getBalance());
    }
}
