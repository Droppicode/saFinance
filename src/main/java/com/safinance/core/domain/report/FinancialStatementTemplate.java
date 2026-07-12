package com.safinance.core.domain.report;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.Transaction;
import com.safinance.core.domain.User;

import java.util.List;

/**
 * Classe base abstrata para geração de extratos financeiros utilizando
 * o padrão de projeto Template Method.
 */
public abstract class FinancialStatementTemplate {

    /**
     * Template Method que define a estrutura (esqueleto) para geração do relatório.
     * 
     * @param user O usuário para o qual o relatório é gerado.
     * @param accounts As contas do usuário a serem incluídas no relatório.
     * @param transactions As transações associadas a essas contas.
     * @return O relatório formatado em String.
     */
    public final String generateReport(User user, List<Account> accounts, List<Transaction> transactions) {
        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append(formatHeader(user, accounts));
        reportBuilder.append("\n");
        reportBuilder.append(formatBody(transactions));
        reportBuilder.append("\n");
        reportBuilder.append(formatFooter(accounts, transactions));

        return reportBuilder.toString();
    }

    /**
     * Formata o cabeçalho do relatório.
     */
    protected abstract String formatHeader(User user, List<Account> accounts);

    /**
     * Formata o corpo principal do relatório, onde as transações geralmente são listadas.
     */
    protected abstract String formatBody(List<Transaction> transactions);

    /**
     * Formata o rodapé do relatório (ex: soma de saldos, totais finais).
     */
    protected abstract String formatFooter(List<Account> accounts, List<Transaction> transactions);
}
