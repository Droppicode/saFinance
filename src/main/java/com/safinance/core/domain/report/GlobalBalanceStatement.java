package com.safinance.core.domain.report;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.IncomeTransaction;
import com.safinance.core.domain.Transaction;
import com.safinance.core.domain.User;

import java.util.List;

/**
 * Extrato global consolidando todas as contas do usuário.
 */
public class GlobalBalanceStatement extends FinancialStatementTemplate {

    @Override
    protected String formatHeader(User user, List<Account> accounts) {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append(" BALANÇO FINANCEIRO GLOBAL\n");
        sb.append("==================================================\n");
        sb.append(String.format("Usuário: %s (%s)\n", user.getName(), user.getEmail()));
        sb.append(String.format("Total de Contas: %d\n", accounts.size()));
        sb.append("--------------------------------------------------");
        return sb.toString();
    }

    @Override
    protected String formatBody(List<Transaction> transactions) {
        double totalIncomes = 0.0;
        double totalExpenses = 0.0;

        for (Transaction tx : transactions) {
            if (tx instanceof IncomeTransaction) {
                totalIncomes += tx.getAmount();
            } else {
                totalExpenses += tx.getAmount();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("  RESUMO DE TRANSAÇÕES NO PERÍODO:\n");
        sb.append(String.format("  - Total de Entradas (Receitas): R$ %.2f\n", totalIncomes));
        sb.append(String.format("  - Total de Saídas (Despesas)  : R$ %.2f\n", totalExpenses));
        return sb.toString();
    }

    @Override
    protected String formatFooter(List<Account> accounts, List<Transaction> transactions) {
        double totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------------\n");
        sb.append(String.format(" PATRIMÔNIO LÍQUIDO CONSOLIDADO: R$ %.2f\n", totalBalance));
        sb.append("==================================================");
        return sb.toString();
    }
}
