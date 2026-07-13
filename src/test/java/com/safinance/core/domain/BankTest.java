package com.safinance.core.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class BankTest {

    private Bank bank;
    private final YearMonth inception = YearMonth.of(2023, 1);
    private final double initialRate = 0.005;

    @BeforeEach
    void setUp() {
        bank = new Bank(inception, initialRate);
    }

    @Test
    void testGetYieldRate_ShouldReturnInitialRateForInceptionMonth() {
        assertEquals(initialRate, bank.getYieldRate(inception), 0.0001, "Deve retornar a taxa inicial");
    }

    @Test
    void testGetYieldRate_FutureMonth_ShouldGenerateRates() {
        YearMonth futureMonth = inception.plusMonths(5);
        
        double rate = bank.getYieldRate(futureMonth);
        
        assertTrue(rate >= 0.004 && rate <= 0.006, "A taxa gerada deve estar entre os limites de negócio");
        assertEquals(futureMonth, bank.getLastKnownMonth(), "O banco deve atualizar o último mês conhecido");
    }

    @Test
    void testGetYieldRate_BeforeInception_ShouldThrowException() {
        YearMonth pastMonth = inception.minusMonths(1);
        
        assertThrows(IllegalArgumentException.class, () -> {
            bank.getYieldRate(pastMonth);
        }, "Não pode gerar taxas antes da criação do banco");
    }

    @Test
    void testOperationCost_WithStrategyPattern() {
        // Por padrão o banco cria a estratégia de TED com 2%
        double amount = 1000.0;
        
        double tedCost = bank.operationCost(amount, "TED");
        assertEquals(20.0, tedCost, 0.01, "O custo do TED deve ser de 2%");
        
        // PIX por padrão é isento
        double pixCost = bank.operationCost(amount, "PIX");
        assertEquals(0.0, pixCost, 0.01, "O custo do PIX deve ser isento");
        
        // Operação não cadastrada deve cair no default (isento)
        double unknownCost = bank.operationCost(amount, "UNKNOWN");
        assertEquals(0.0, unknownCost, 0.01, "Operação desconhecida deve ser isenta");
    }

    @Test
    void testSetOperationTax_ShouldChangeStrategyDynamically() {
        double amount = 1000.0;
        bank.setOperationTax("PIX", 0.01); // 1%
        
        double pixCost = bank.operationCost(amount, "PIX");
        
        assertEquals(10.0, pixCost, 0.01, "O custo do PIX deve ter mudado para 1%");
    }
}
