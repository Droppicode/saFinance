package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.core.domain.CreditAccount;
import com.safinance.core.domain.Role;
import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.domain.User;
import com.safinance.core.domain.WalletAccount;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;


public class ManageAccountsMenu implements BaseMenu {

    private final User user;
    // Se o usuário logado for admin, accountOwner representa o usuário cujas contas
    // estão sendo gerenciadas. Para um usuário regular, é normalmente o próprio usuário logado.
    private final User accountOwner;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public ManageAccountsMenu(User user, User accountOwner, UserUseCase userUseCase, BankUseCase bankUseCase, AccountUseCase accountUseCase) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;

        registerTransition("1", () -> new CreateAccountMenu(user, accountOwner, userUseCase, bankUseCase, accountUseCase), transitions);
        registerTransition("2", () -> this, transitions);
        registerTransition("3", () -> new AccountSelectionMenu(user, accountOwner, userUseCase, bankUseCase, accountUseCase), transitions);
        if (user.getRole() == Role.REGULAR) {
            registerTransition("0", () -> new UserMenu(user, accountUseCase), transitions);
        } else {
            registerTransition("0", () -> new UserSelectionMenu(user, bankUseCase, userUseCase, accountUseCase), transitions);
        }
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Gerenciar Contas");
        promptService.printInfo("Contas do usuário: " + accountOwner.getName());
        promptService.printInfo("");
        
        var accounts = accountUseCase.listUserAccounts(accountOwner);
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
        return new ArrayList<>(transitions.keySet());
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha uma opção: ").trim();
        Supplier<BaseMenu> transition = transitions.get(option);

        if (transition != null) {
            if (option.equals("2")) {
                promptService.printWarning("Em desenvolvimento: Funcionalidade ainda não implementada.");
                promptService.readString("Pressione Enter para tentar novamente.");
            }
            return transition.get();
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }
    
}
