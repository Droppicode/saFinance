package com.safinance.core.domain;

import java.time.YearMonth;
import com.safinance.core.domain.Bank;

public class BankUseCase {

    private final Bank bank;

    public BankUseCase(Bank bank) {
        this.bank = bank;
    }

    public void updateYieldRate(YearMonth month, double newRate) {
        bank.setYieldRate(month, newRate);
    }

    public void updateOperationTax(String operationType, double newRate) {
        bank.setOperationTax(operationType, newRate);
    }
}