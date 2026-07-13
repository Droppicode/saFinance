package com.safinance.view;

import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.InvestmentUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.core.usecases.UserUseCase;

/**
 * Agrupa todas as dependências de caso de uso compartilhadas entre menus e ações.
 * Substitui a explosão de parâmetros nos construtores da camada View.
 */
public record MenuContext(
    AuthUseCase authUseCase,
    UserUseCase userUseCase,
    BankUseCase bankUseCase,
    AccountUseCase accountUseCase,
    InvestmentUseCase investmentUseCase,
    TransactionUseCase transactionUseCase
) {}
