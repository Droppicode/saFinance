package com.safinance.view;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Scanner;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.Transaction;
import com.safinance.core.domain.WalletAccount;

/**
 * Menu interativo rudimentar para testar a mecânica imutável das Contas no terminal.
 */
public class TestMenu {
    
    // Transação falsa local (Dummy/Stub) apenas para testes, pois não temos a real ainda
    private record DummyTransaction(double amount) implements Transaction {
        @Override
        public String getId() { return "TX_TEST"; }
        @Override
        public double getAmount() { return amount; }
        @Override
        public LocalDateTime getDate() { return LocalDateTime.now(); }
        @Override
        public String getDescription() { return "Transação de Teste"; }
        @Override
        public String getAccountId() { return "ACC_1"; }
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        
        // 1. Criamos a conta com R$ 1000 de saldo inicial
        Account myAccount = new WalletAccount("ACC_1", "USER_1", 1000.0, Collections.emptyMap());

        while (true) {
            System.out.println("\n==================================");
            System.out.println("   TESTE DE CONTAS (Imutáveis)");
            System.out.println("==================================");
            System.out.println("Tipo de Conta: " + myAccount.getClass().getSimpleName());
            System.out.println("Saldo Atual:   R$ " + myAccount.getBalance());
            System.out.println("----------------------------------");
            System.out.println("1 - Depositar R$ 500");
            System.out.println("2 - Sacar R$ 200 (Gasto)");
            System.out.println("3 - Tentar sacar R$ 5000 (Testar Limites / Fail-Fast)");
            System.out.println("0 - Voltar/Sair");
            System.out.print("> Escolha uma opção: ");
            
            String choice = scanner.nextLine();
            
            try {
                if ("1".equals(choice)) {
                    // Wither Pattern: A variável 'myAccount' passa a apontar para a NOVA conta gerada
                    myAccount = myAccount.process(new DummyTransaction(500.0));
                    System.out.println("✅ Depósito processado! O saldo deve ser atualizado.");
                } else if ("2".equals(choice)) {
                    // Um gasto é processado como valor negativo
                    myAccount = myAccount.process(new DummyTransaction(-200.0));
                    System.out.println("✅ Saque processado! O saldo deve ser reduzido.");
                } else if ("3".equals(choice)) {
                    // Isso deve estourar a Exception que fizemos na WalletAccount
                    myAccount = myAccount.process(new DummyTransaction(-5000.0));
                    System.out.println("✅ Saque realizado (Você não deveria ver esta mensagem!)."); 
                } else if ("0".equals(choice)) {
                    System.out.println("Encerrando testes de conta...");
                    break;
                } else {
                    System.out.println("⚠️ Opção inválida.");
                }
            } catch (IllegalArgumentException e) {
                // Capturamos a regra de negócio que estourou e mostramos pro usuário
                System.out.println("\n❌ REGRA DE NEGÓCIO BARRADA: " + e.getMessage());
            }
        }
    }
}
