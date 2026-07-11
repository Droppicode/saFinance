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
    public void showMenu() {
        while (true) {
            
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
                    new ManageAccountsMenu(user, accountUseCase).showMenu();
                break;
            case 2:
                System.out.println("Em desenvolvimento: Extrato financeiro ainda não implementado.");
                System.out.println("Pressione Enter para tentar novamente.");
                scanner.nextLine();
                clearScreen();
                showMenu();
                break;
            case 3:
                System.out.println("Em desenvolvimento: Investimentos ainda não implementados.");
                System.out.println("Pressione Enter para tentar novamente.");
                scanner.nextLine();
                clearScreen();
                showMenu();
                break;
                case 0:
                    System.out.println("Encerrando sessão. Até logo!");
                    System.exit(0); // Sai do programa após o usuário sair do menu
                    default:
                        System.out.println("Opção inválida.");
                        System.out.println("Pressione Enter para tentar novamente.");
                        scanner.nextLine();
                        clearScreen();
                        showMenu();
                        break;
                    }
                }
            }   
}
