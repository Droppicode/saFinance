package com.safinance.view;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;

/**
 * Menu principal para usuários não administradores.
 */
public class UserMenu extends BaseMenu {

    // Construtor da classe.
    private final User user;    
    private final AccountUseCase accountUseCase;

    /**
     * Construtor da classe.
     * @param user O usuário logado.
     * @param accountUseCase A instância do caso de uso de contas.
     */
    public UserMenu(User user, AccountUseCase accountUseCase) {
        this.user = user;
        this.accountUseCase = accountUseCase;
    }

    /**
     * Exibe o menu do usuário.
     */
    @Override
    public BaseMenu render() {
            printHeader("Menu do Usuário");
            System.out.println("Bem-vindo, " + user.getName() + "!");
            printOptions(
                "Gerenciar contas",
                "Extrato financeiro",
                "Investimentos"
            );
            
            int option = readOption();
            switch (option) {
                case 1:
                    return new ManageAccountsMenu(user, accountUseCase);
                case 2:
                    System.out.println("Em desenvolvimento: Extrato financeiro ainda não implementado.");
                    System.out.println("Pressione Enter para tentar novamente.");
                    scanner.nextLine();
                    return this;
                case 3:
                    System.out.println("Em desenvolvimento: Investimentos ainda não implementados.");
                    System.out.println("Pressione Enter para tentar novamente.");
                    scanner.nextLine();
                    return this;
                case 0:
                    System.out.println("Encerrando sessão. Até logo!");
                    return null; // Return null to exit the application or go back to login (Main will handle)
                default:
                    System.out.println("Opção inválida.");
                    System.out.println("Pressione Enter para tentar novamente.");
                    scanner.nextLine();
                    return this;
            }
    }   
}
