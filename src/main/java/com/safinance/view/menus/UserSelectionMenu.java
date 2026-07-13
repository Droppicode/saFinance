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
 * Menu para seleção de usuários, permitindo que o administrador escolha um usuário específico para gerenciar suas contas.
 */
public class UserSelectionMenu implements BaseMenu {

    private final User user;
    private final MenuContext ctx;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    /**
     * Construtor da classe.
     * @param user O usuário logado (administrador).
     * @param ctx O contexto com todas as dependências de caso de uso.
     */
    public UserSelectionMenu(User user, MenuContext ctx) {
        this.user = user;
        this.ctx = ctx;
    }

    /**
     * Renderiza o cabeçalho do menu.
     * @param promptService A instância do serviço de prompt.
     */
    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Seleção de Usuário");
        promptService.printInfo("");

        var users = ctx.userUseCase().getAllUsers();
        if (users.isEmpty()) {
            promptService.printWarning("Nenhum usuário encontrado.\n");
            return;
        }

        int index = 1;
        for (User u : users) {
            promptService.printInfo(index + ". " + u.getName() + " (" + u.getEmail() + ")");
            index++;
        }

        promptService.printInfo("0. Voltar\n");
    }
    
    @Override
    public List<String> getOptions() {
        return new ArrayList<>(transitions.keySet());
    }

    /**
     * Manipula a entrada do usuário.
     * As transições dinâmicas são construídas aqui (não no renderHeader) para evitar side effects no render.
     * @param promptService A instância do serviço de prompt.
     * @return O menu correspondente à opção escolhida.
     */
    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha o usuário pelo índice ou digite '0' para voltar: ").trim();

        if (option.equals("0")) {
            return new ManageUsersMenu(user, ctx);
        }

        var users = ctx.userUseCase().getAllUsers();
        try {
            int index = Integer.parseInt(option) - 1;
            if (index >= 0 && index < users.size()) {
                User selectedUser = users.get(index);
                return new ManageAccountsMenu(user, selectedUser, ctx);
            }
        } catch (NumberFormatException ignored) {}

        promptService.printError("Opção inválida.");
        promptService.readString("Pressione Enter para tentar novamente.");
        return this;
    }
}
