package com.safinance.view.menus;

import com.safinance.core.domain.User;
import com.safinance.core.domain.WalletAccount;
import com.safinance.view.AbstractMenu;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;

public class ManageAccountsMenu extends AbstractMenu {

    private final User user;
    private final User accountOwner;
    private final MenuContext ctx;

    public ManageAccountsMenu(User user, User accountOwner, MenuContext ctx) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.ctx = ctx;

        registerCommand("1", "Criar nova conta", prompt -> new CreateAccountMenu(user, accountOwner, ctx));
        registerCommand("2", "Depositar / Retirar / Transferir", prompt -> new TransactionMenu(user, accountOwner, ctx));
        registerCommand("3", "Simular rendimento (para contas poupança)", prompt -> new AccountSelectionMenu(user, accountOwner, ctx));
        registerCommand("4", "Gerenciar investimentos (apenas para contas Carteira)", this::handleInvestmentTransition);
        
        if (user.equals(accountOwner)) {
            registerCommand("0", "Voltar", prompt -> new UserMenu(user, ctx));
        } else {
            registerCommand("0", "Voltar", prompt -> new UserSelectionMenu(user, ctx));
        }
    }

    @Override
    protected void printHeader(PromptService promptService) {
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
        var wallet = ctx.investmentUseCase().getWalletAccountByUser(accountOwner).orElse(null);
        if (wallet != null) {
            promptService.printInfo(String.format("Conta carteira existente: saldo R$ %.2f | %d posições", wallet.getBalance(), wallet.getPortfolio().size()));
        } else {
            promptService.printInfo("Nenhuma conta carteira encontrada. Você pode criar uma no menu de contas.");
        }

        promptService.printInfo("");
    }

    private BaseMenu handleInvestmentTransition(PromptService promptService) {
        var wallets = ctx.investmentUseCase().getWalletAccountsByUser(accountOwner);

        if (wallets == null || wallets.isEmpty()) {
            return new InvestmentMenu(accountOwner, ctx, this, null);
        } else if (wallets.size() == 1) {
            return new InvestmentMenu(accountOwner, ctx, this, wallets.getFirst().getName());
        } else {
            var walletsStrings = wallets.stream().map(w -> w.getName()).toList();
            String selectedWalletName = promptService.readWithOptions("Selecione a carteira de investimentos: ", walletsStrings);
            WalletAccount wallet = ctx.investmentUseCase().getWalletAccountByUserAndName(accountOwner, selectedWalletName).orElse(null);
            if (wallet == null) {
                return this;
            }
            return new InvestmentMenu(accountOwner, ctx, this, selectedWalletName);
        }
    }
}
