package com.safinance.view;

import com.safinance.core.domain.CreditAccount;
import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.domain.User;
import com.safinance.core.domain.WalletAccount;
import com.safinance.core.usecases.AccountUseCase;

public class ManageAccountsMenu extends BaseMenu {

    private final User user;
    private final AccountUseCase accountUseCase;

    public ManageAccountsMenu(User user, AccountUseCase accountUseCase) {
        this.user = user;
        this.accountUseCase = accountUseCase;
    }

    @Override
    public BaseMenu render() {
        printHeader("Gerenciar Contas");
        System.out.println("Contas do usuário: " + user.getName());
        System.out.println();
        
        var accounts = accountUseCase.listUserAccounts(user);
        if (accounts.isEmpty()) {
            System.out.println("Nenhuma conta encontrada para este usuário.");
        } else {
            System.out.printf("%-12s | %-10s | %-10s%n",
                "Tipo", "Saldo", "Limite");
            System.out.println("---------------------------------------------");
            for (var account : accounts) {
                if (account instanceof SavingsAccount sa) {
                    System.out.printf("%-12s | %-10.2f | %-10s%n",
                        "Poupança", sa.getBalance(), "-");
                } else if (account instanceof WalletAccount wa) {
                    System.out.printf("%-12s | %-10.2f | %-10s%n",
                        "Carteira", wa.getBalance(), "-");
                } else if (account instanceof CreditAccount ca) {
                    System.out.printf("%-12s | %-10.2f | %-10.2f%n",
                        "Crédito", ca.getBalance(), ca.getCreditLimit());
                } else {
                    System.out.printf("%-12s | %-10s | %-10s%n",
                        "Desconhecida", "-", "-");
                }
            }
        }

        System.out.println();
        printOptions(
        "Criar nova conta",
                    "Depositar / Retirar / Transferir",
                    "Aplicar rendimento (para contas poupança)"
        );
        int option = readOption();

        switch (option) {
            case 1:
                return new CreateAccountMenu(user, accountUseCase);
            case 2:
                System.out.println("Em desenvolvimento: Função de depósito/retirada/transferência ainda não implementada.");
                System.out.println("Pressione Enter para tentar novamente.");
                scanner.nextLine();
                return this;
            case 3:
                System.out.println("Em desenvolvimento: Função de aplicar rendimento ainda não implementada.");
                System.out.println("Pressione Enter para tentar novamente.");
                scanner.nextLine();
                return this;
            case 0:
                return new UserMenu(user, accountUseCase);
            default:
                System.out.println("Opção inválida.");
                System.out.println("Pressione Enter para retornar.");
                scanner.nextLine();
                return this;
        }
    }
    
}
