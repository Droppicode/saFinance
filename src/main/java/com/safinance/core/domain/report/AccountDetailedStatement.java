package com.safinance.core.domain.report;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.IncomeTransaction;
import com.safinance.core.domain.Transaction;
import com.safinance.core.domain.User;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Extrato detalhado focado em uma única conta.
 */
public class AccountDetailedStatement extends FinancialStatementTemplate {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    protected String formatHeader(User user, List<Account> accounts) {
        if (accounts.isEmpty()) {
            return "Nenhuma conta selecionada para o extrato.";
        }
        
        Account account = accounts.get(0); // Para o extrato detalhado, foca na primeira conta
        
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append(" EXTRATO DETALHADO DE CONTA\n");
        sb.append("==================================================\n");
        sb.append(String.format("Titular: %s\n", user.getName()));
        sb.append(String.format("Conta ID: %s\n", account.getId()));
        sb.append(String.format("Tipo: %s\n", account.getAccountType()));
        sb.append("--------------------------------------------------");
        return sb.toString();
    }

    @Override
    protected String formatBody(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "  Nenhuma transação encontrada neste período.\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-18s | %-12s | %-15s | %s\n", "Data", "Tipo", "Valor", "Descrição"));
        sb.append("--------------------------------------------------\n");

        transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .forEach(tx -> {
                    String type = tx.isIncome() ? "ENTRADA" : "SAÍDA";
                    String date = tx.getDate().format(DATE_FORMATTER);
                    // O valor é sempre positivo na interface, mas para exibição fica visual se é saída/entrada
                    String amountPrefix = tx.isIncome() ? "+ " : "- ";
                    
                    sb.append(String.format("%-18s | %-12s | %sR$ %-12.2f | %s\n",
                            date, type, amountPrefix, tx.getAmount(), tx.getDescription()));
                });

        return sb.toString();
    }

    @Override
    protected String formatFooter(List<Account> accounts, List<Transaction> transactions) {
        if (accounts.isEmpty()) return "";
        
        Account account = accounts.get(0);
        
        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------------\n");
        sb.append(String.format(" SALDO ATUAL DA CONTA: R$ %.2f\n", account.getBalance()));
        sb.append("==================================================");
        return sb.toString();
    }
}
