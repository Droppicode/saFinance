package com.safinance.view;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import java.util.List;
import java.util.Arrays;

public class CreateAccountMenu implements BaseMenu {

    private final User user;
    private final AccountUseCase accountUseCase;

    public CreateAccountMenu(User user, AccountUseCase accountUseCase) {
        this.user = user;
        this.accountUseCase = accountUseCase;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Criar Nova Conta");
        promptService.printMenuOptions("Corrente", "Poupança", "Credito");
    }

    @Override
    public List<String> getOptions() {
        return Arrays.asList("1", "2", "3", "0");
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        int option = promptService.readIntOption("> Escolha uma opção: ");
        String accountType;

        switch (option) {
            case 1:
                accountUseCase.createWalletAccount(user, 0.0, null); 
                // Cria uma conta corrente com saldo inicial de 0.0
                // Usuário padão não define o balanço inicial nem o banco, pois isso é feito pelo administrador.
                promptService.printSuccess("Conta corrente criada com sucesso!");
                promptService.readString("Pressione Enter para voltar ao menu anterior.");
                return new ManageAccountsMenu(user, accountUseCase);
            case 2:
                accountUseCase.createSavingsAccount(user, 0.0);
                promptService.printSuccess("Conta poupança criada com sucesso!");
                promptService.readString("Pressione Enter para voltar ao menu anterior.");
                return new ManageAccountsMenu(user, accountUseCase);
            case 3:
                double creditLimit = -1;
                String input = promptService.readString("Qual será o limite de crédito da conta? ");
                try {
                    creditLimit = Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    promptService.printError("Valor de limite inválido.");
                    promptService.readString("Pressione Enter para tentar novamente.");
                    return this;
                }
                
                try {
                    accountUseCase.createCreditAccount(user, 0.0, creditLimit);
                    promptService.printSuccess("Conta de crédito criada com sucesso!");
                } catch (Exception e) {
                    promptService.printError("Erro ao criar conta de crédito: " + e.getMessage());
                }
                promptService.readString("Pressione Enter para voltar ao menu anterior.");
                return new ManageAccountsMenu(user, accountUseCase);
            
            case 0:
                return new ManageAccountsMenu(user, accountUseCase);

            default:
                promptService.printError("Opção inválida.");
                promptService.readString("Pressione Enter para tentar novamente.");
                return this;
        }
    }
    
}
