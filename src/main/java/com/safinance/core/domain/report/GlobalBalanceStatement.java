package com.safinance.core.domain.report;

import com.safinance.core.domain.Account;
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
        double internalTransfersPrincipal = 0.0;

        for (Transaction tx : transactions) {
            boolean isInternalTransfer = tx.isTransfer();

            if (tx.isIncome()) {
                totalIncomes += tx.getAmount();
                if (isInternalTransfer) {
                    internalTransfersPrincipal += tx.getAmount();
                }
            } else {
                totalExpenses += tx.getAmount();
            }
        }

        // Para evitar inflar os totais com movimentações internas entre contas do mesmo usuário,
        // subtraímos o "principal" das transferências das receitas.
        // Como as Despesas retornam getAmount() negativo, nós SOMAMOS o principal para anular o valor inflado.
        // (Ex: -510 + 500 = -10 de taxa real).
        totalIncomes -= internalTransfersPrincipal;
        totalExpenses += internalTransfersPrincipal;

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
