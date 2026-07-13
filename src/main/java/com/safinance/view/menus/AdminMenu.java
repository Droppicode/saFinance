package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.core.domain.User;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;

/**
 * Menu principal para usuários administradores.
 */
public class AdminMenu implements BaseMenu {

    private final User user;
    private final MenuContext ctx;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    /**
     * Construtor da classe.
     * @param user O usuário logado.
     * @param ctx O contexto com todas as dependências de caso de uso.
     */
    public AdminMenu(User user, MenuContext ctx) {
        this.user = user;
        this.ctx = ctx;

        registerTransition("1", () -> new ManageUsersMenu(user, ctx), transitions);
        registerTransition("2", () -> new ManageAccountsMenu(user, user, ctx), transitions);
        registerTransition("3", () -> new ManageBanksMenu(user, ctx), transitions);
        registerTransition("0", () -> new UserMenu(user, ctx), transitions);
    }

    /**
     * Exibe o menu do administrador.
     */
    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Menu do Administrador");
        promptService.printInfo("Bem-vindo, " + user.getName() + "!");
        promptService.printMenuOptions(
            "Gerenciar usuários",
            "Gerenciar contas",
            "Gerenciar bancos"
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
            if (option.equals("0")) {
                promptService.printSuccess("Voltando ao menu do usuário...");
            }
            return transition.get();
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }   
}
