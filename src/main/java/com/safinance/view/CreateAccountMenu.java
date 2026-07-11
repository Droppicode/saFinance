package com.safinance.view;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;

public class CreateAccountMenu extends BaseMenu {

    private final User user;
    private final AccountUseCase accountUseCase;

    public CreateAccountMenu(User user, AccountUseCase accountUseCase) {
        this.user = user;
        this.accountUseCase = accountUseCase;
    }

    @Override
    public BaseMenu render() {
        printHeader("Criar Nova Conta");
        printOptions("Corrente", "Poupança", "Credito");
        int option = readOption();
        String accountType;

        switch (option) {
            case 1:
                accountUseCase.createWalletAccount(user, 0.0, null); 
                // Cria uma conta corrente com saldo inicial de 0.0
                // Usuário padão não define o balanço inicial nem o banco, pois isso é feito pelo administrador.
                System.out.println("Conta corrente criada com sucesso!");
                System.out.println("Pressione Enter para voltar ao menu anterior.");
                scanner.nextLine();
                return new ManageAccountsMenu(user, accountUseCase);
            case 2:
                accountUseCase.createSavingsAccount(user, 0.0);
                System.out.println("Conta poupança criada com sucesso!");
                System.out.println("Pressione Enter para voltar ao menu anterior.");
                scanner.nextLine();
                return new ManageAccountsMenu(user, accountUseCase);
            case 3:
                System.out.println("Qual será o limite de crédito da conta?");
                double creditLimit = scanner.nextDouble();
                scanner.nextLine(); // Consome o newline pendente do nextDouble
                try {
                    accountUseCase.createCreditAccount(user, 0.0, creditLimit);
                    System.out.println("Conta de crédito criada com sucesso!");
                } catch (Exception e) {
                    System.out.println("Erro ao criar conta de crédito." + e.getMessage());
                }
                System.out.println("Pressione Enter para voltar ao menu anterior.");
                scanner.nextLine();
                return new ManageAccountsMenu(user, accountUseCase);
            
            case 0:
                return new ManageAccountsMenu(user, accountUseCase);

            default:
                System.out.println("Opção inválida.");
                System.out.println("Pressione Enter para tentar novamente.");
                scanner.nextLine();
                return this;
        }
    }
    
}
