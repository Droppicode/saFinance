package com.safinance.view.menus;

import com.safinance.view.AbstractMenu;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;

import com.safinance.core.domain.User;
import com.safinance.core.domain.WalletAccount;
import com.safinance.core.domain.UserVisitor;
import com.safinance.core.domain.AdminUser;
import com.safinance.core.domain.RegularUser;

/**
 * Menu principal para usuários.
 * Usa o padrão Visitor para adicionar opções específicas de Admin (Polimorfismo).
 */
public class UserMenu extends AbstractMenu {

    private final User user;    
    private final MenuContext ctx;

    /**
     * Construtor da classe.
     * @param user O usuário logado.
     * @param ctx O contexto com todas as dependências de caso de uso.
     */
    public UserMenu(User user, MenuContext ctx) {
        this.user = user;
        this.ctx = ctx;

        registerCommand("1", "Gerenciar contas", prompt -> new ManageAccountsMenu(user, user, ctx));
        registerCommand("2", "Extrato financeiro", prompt -> new ReportMenu(user, ctx, this));
        registerCommand("3", "Investimentos", prompt -> handleInvestmentTransition(prompt));
        
        // Double Dispatch via Visitor para resolver o menu do Admin
        user.accept(new UserVisitor<Void>() {
            @Override
            public Void visitAdmin(AdminUser admin) {
                registerCommand("4", "Painel Administrativo", prompt -> new AdminMenu(admin, ctx));
                return null;
            }

            @Override
            public Void visitRegular(RegularUser regular) {
                return null;
            }
        });

        registerCommand("0", "Sair", prompt -> {
            prompt.printSuccess("Encerrando sessão. Até logo!");
            return new WelcomeMenu(ctx);
        });
    }

    @Override
    protected void printHeader(PromptService promptService) {
        promptService.printHeader("Menu do Usuário");
        promptService.printInfo("Bem-vindo, " + user.getName() + "!");
    }

    private BaseMenu handleInvestmentTransition(PromptService promptService) {
        var wallets = ctx.investmentUseCase().getWalletAccountsByUser(user);

        if (wallets == null || wallets.isEmpty()) {
            return new InvestmentMenu(user, ctx, this, null);
        } else if (wallets.size() == 1) {
            return new InvestmentMenu(user, ctx, this, wallets.getFirst().getName());
        } else {
            var walletsStrings = wallets.stream().map(w -> w.getName()).toList();
            String selectedWalletName = promptService.readWithOptions("Selecione a carteira de investimentos: ", walletsStrings);
            WalletAccount wallet = ctx.investmentUseCase().getWalletAccountByUserAndName(user, selectedWalletName).orElse(null);
            if (wallet == null) {
                return this;
            }
            return new InvestmentMenu(user, ctx, this, selectedWalletName);
        }
    }
}