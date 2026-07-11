package com.safinance.view;

import com.safinance.core.domain.WalletAccount;
import com.safinance.core.domain.CreditAccount;
import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;

import java.util.List;
import java.util.Arrays;

public class ManageAccountsMenu implements BaseMenu {

    private final User user;
    private final AccountUseCase accountUseCase;

    public ManageAccountsMenu(User user, AccountUseCase accountUseCase) {
        this.user = user;
        this.accountUseCase = accountUseCase;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Gerenciar Contas");
        promptService.printInfo("Contas do usuário: " + user.getName());
        promptService.printInfo("");
        
        var accounts = accountUseCase.listUserAccounts(user);
        if (accounts.isEmpty()) {
            promptService.printWarning("Nenhuma conta encontrada para este usuário.");
        } else {
            promptService.printInfo(String.format("%-12s | %-10s | %-10s", "Tipo", "Saldo", "Limite"));
            promptService.printInfo("---------------------------------------------");
            for (var account : accounts) {
                if (account instanceof SavingsAccount sa) {
                    promptService.printInfo(String.format("%-12s | %-10.2f | %-10s", "Poupança", sa.getBalance(), "-"));
                } else if (account instanceof WalletAccount wa) {
                    promptService.printInfo(String.format("%-12s | %-10.2f | %-10s", "Carteira", wa.getBalance(), "-"));
                } else if (account instanceof CreditAccount ca) {
                    promptService.printInfo(String.format("%-12s | %-10.2f | %-10.2f", "Crédito", ca.getBalance(), ca.getCreditLimit()));
                } else {
                    promptService.printInfo(String.format("%-12s | %-10s | %-10s", "Desconhecida", "-", "-"));
                }
            }
        }

        promptService.printInfo("");
        promptService.printMenuOptions(
        "Criar nova conta",
                    "Depositar / Retirar / Transferir",
                    "Aplicar rendimento (para contas poupança)"
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
                return new CreateAccountMenu(user, accountUseCase);
            case 2:
                promptService.printWarning("Em desenvolvimento: Função de depósito/retirada/transferência ainda não implementada.");
                promptService.readString("Pressione Enter para tentar novamente.");
                return this;
            case 3:
                promptService.printWarning("Em desenvolvimento: Função de aplicar rendimento ainda não implementada.");
                promptService.readString("Pressione Enter para tentar novamente.");
                return this;
            case 0:
                return new UserMenu(user, accountUseCase);
            default:
                promptService.printError("Opção inválida.");
                promptService.readString("Pressione Enter para retornar.");
                return this;
        }
    }
    
}
