package com.safinance.view;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;

import java.util.List;
import java.util.Arrays;

/**
 * Menu principal para usuários não administradores.
 */
public class UserMenu implements BaseMenu {

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
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Menu do Usuário");
        promptService.printInfo("Bem-vindo, " + user.getName() + "!");
        promptService.printMenuOptions(
            "Gerenciar contas",
            "Extrato financeiro",
            "Investimentos"
        );
    }

    @Override
    public List<String> getOptions() {
        return Arrays.asList("1", "2", "3", "0");
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        int option = promptService.readIntOption("> Escolha uma opção: ");
        switch (option) {
            case 1:
                return new ManageAccountsMenu(user, accountUseCase);
            case 2:
                promptService.printWarning("Em desenvolvimento: Extrato financeiro ainda não implementado.");
                promptService.readString("Pressione Enter para tentar novamente.");
                return this;
            case 3:
                promptService.printWarning("Em desenvolvimento: Investimentos ainda não implementados.");
                promptService.readString("Pressione Enter para tentar novamente.");
                return this;
            case 0:
                promptService.printSuccess("Encerrando sessão. Até logo!");
                return null; // Return null to exit the application or go back to login (Main will handle)
            default:
                promptService.printError("Opção inválida.");
                promptService.readString("Pressione Enter para tentar novamente.");
                return this;
        }
    }   
}
