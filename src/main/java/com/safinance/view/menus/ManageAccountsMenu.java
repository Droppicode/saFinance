package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.core.domain.Role;
import com.safinance.core.domain.User;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;


public class ManageAccountsMenu implements BaseMenu {

    private final User user;
    // Se o usuário logado for admin, accountOwner representa o usuário cujas contas
    // estão sendo gerenciadas. Para um usuário regular, é normalmente o próprio usuário logado.
    private final User accountOwner;
    private final MenuContext ctx;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public ManageAccountsMenu(User user, User accountOwner, MenuContext ctx) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.ctx = ctx;

        registerTransition("1", () -> new CreateAccountMenu(user, accountOwner, ctx), transitions);
        registerTransition("2", () -> new TransactionMenu(user, accountOwner, ctx), transitions);
        registerTransition("3", () -> new AccountSelectionMenu(user, accountOwner, ctx), transitions);
        if (user.getRole() == Role.REGULAR) {
            registerTransition("0", () -> new UserMenu(user, ctx), transitions);
        } else {
            registerTransition("0", () -> new UserSelectionMenu(user, ctx), transitions);
        }
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Gerenciar Contas");
        promptService.printInfo("Contas do usuário: " + accountOwner.getName());
        promptService.printInfo("");
        
        var accounts = ctx.accountUseCase().listUserAccounts(accountOwner);
        if (accounts.isEmpty()) {
            promptService.printWarning("Nenhuma conta encontrada para este usuário.");
        } else {
            promptService.printInfo(String.format("%-15s | %-12s | %-10s | %-10s", "Nome", "Tipo", "Saldo", "Limite"));
            promptService.printInfo("---------------------------------------------------------");
            for (var account : accounts) {
                promptService.printInfo(account.getDisplaySummary());
            }
        }

        promptService.printInfo("");
        var wallet = ctx.investmentUseCase().getWalletAccount(user);
        if (wallet != null) {
            promptService.printInfo(String.format("Conta carteira existente: saldo R$ %.2f | %d posições", wallet.getBalance(), wallet.getPortfolio().size()));
        } else {
            promptService.printInfo("Nenhuma conta carteira encontrada. Você pode criar uma no menu de contas.");
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
            return transition.get();
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }
    
}
