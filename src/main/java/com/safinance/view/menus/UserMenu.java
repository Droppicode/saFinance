package com.safinance.view.menus;

import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;

import com.safinance.core.domain.User;
import com.safinance.core.domain.WalletAccount;
import com.safinance.core.domain.Role;
import com.safinance.core.domain.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Menu principal para usuários não administradores.
 */
public class UserMenu implements BaseMenu {

    private final User user;    
    private final MenuContext ctx;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    /**
     * Construtor da classe.
     * @param user O usuário logado.
     * @param ctx O contexto com todas as dependências de caso de uso.
     */
    public UserMenu(User user, MenuContext ctx) {
        this.user = user;
        this.ctx = ctx;

        registerTransition("1", () -> new ManageAccountsMenu(user, user, ctx), transitions);
        registerTransition("2", () -> new ReportMenu(user, ctx, this), transitions);
        registerTransition("3", () -> new InvestmentMenu(user, ctx, this), transitions);
        if (user.getRole() == Role.ADMIN) {
            registerTransition("4", () -> new AdminMenu(user, ctx), transitions);
        }
        registerTransition("0", () -> new WelcomeMenu(ctx), transitions);
    }

    /**
     * Exibe o menu do usuário.
     */
    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Menu do Usuário");
        promptService.printInfo("Bem-vindo, " + user.getName() + "!");
        if (user.getRole() == Role.ADMIN) {
            promptService.printMenuOptions(
                "Gerenciar contas",
                "Extrato financeiro",
                "Investimentos",
                "Painel Administrativo"
            );
        } else {
            promptService.printMenuOptions(
                "Gerenciar contas",
                "Extrato financeiro",
                "Investimentos"
            );
        }
    }

    @Override
    public List<String> getOptions() {
        return new ArrayList<>(transitions.keySet());
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha uma opção: ").trim();
        
        // Trata a transição para Investimentos de forma especial
        if (option.equals("3")) {
            return handleInvestmentTransition(promptService);
        }
        
        Supplier<BaseMenu> transition = transitions.get(option);

        if (transition != null) {
            if (option.equals("0")) {
                promptService.printSuccess("Encerrando sessão. Até logo!");
            }
            return transition.get();
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
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
            WalletAccount wallet = ctx.investmentUseCase().getWalletAccountByUserAndName(user, selectedWalletName);
            if (wallet == null) {
                return this;
            }
            return new InvestmentMenu(user, ctx, this, selectedWalletName);
        }
    }
}