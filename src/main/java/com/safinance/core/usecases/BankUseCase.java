package com.safinance.core.usecases;

import java.time.YearMonth;
import com.safinance.core.domain.Bank;
import com.safinance.core.ports.Repository;

public class BankUseCase {

    private final Bank bank;
    private final Repository<Bank, String> repository;

    public BankUseCase(Bank bank, Repository<Bank, String> repository) {
        if (bank == null) throw new IllegalArgumentException("O banco não pode ser nulo.");
        this.bank = bank;
        this.repository = repository;
    }

    public void updateYieldRate(YearMonth month, double newRate) {
        bank.setYieldRate(month, newRate);
        if (repository != null) repository.save(bank);
    }

    public void updateOperationTax(String operationType, double newRate) {
        bank.setOperationTax(operationType, newRate);
        if (repository != null) repository.save(bank);
    }

    public YearMonth getCurrentSimulationMonth() {
        return bank.getLastKnownMonth();
    }

    public YearMonth getInceptionMonth() {
        return bank.getInceptionMonth();
    }

    public double getYieldRate(YearMonth month) {
        YearMonth before = bank.getLastKnownMonth();
        double rate = bank.getYieldRate(month);
        
        // Se a chamada getYieldRate forçou o banco a gerar novos meses aleatórios
        // para cobrir buracos no tempo, o estado mudou. Precisamos persistir.
        if (repository != null && !bank.getLastKnownMonth().equals(before)) {
            repository.save(bank);
        }
        
        return rate;
    }
}