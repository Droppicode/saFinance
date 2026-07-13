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
import com.safinance.view.actions.RegisterAction;

/**
 * Menu para gerenciar usuários do sistema.
 */
public class ManageUsersMenu implements BaseMenu {

    private final User user;
    private final MenuContext ctx;
    
    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    /**
     * Construtor do menu de gerenciamento de usuários.
     *
     * @param user O usuário logado
     * @param ctx O contexto com todas as dependências de caso de uso
     */
    public ManageUsersMenu(User user, MenuContext ctx) {
        this.user = user;
        this.ctx = ctx;

        // Registra as opções do menu e as transições correspondentes.
        registerTransition("1", () -> new RegisterAction(user, ctx.userUseCase(), () -> this), transitions);
        registerTransition("2", () -> new UserSelectionMenu(user, ctx), transitions);
        registerTransition("0", () -> new AdminMenu(user, ctx), transitions);
    }

    /**
     * Renderiza o cabeçalho do menu com a lista de usuários disponíveis.
     * Verifica se o usuário tem permissão de administrador antes de exibir o conteúdo.
     *
     * @param promptService Serviço para exibir mensagens ao usuário
     */
    @Override
    public void renderHeader(PromptService promptService) {
        // Verifica se o usuário é um administrador
        if (user == null || user.getRole() != Role.ADMIN) {
            promptService.printError("Acesso negado. Apenas administradores podem gerenciar usuários.");
            return;
        }

        promptService.printHeader("Gerenciar Usuários");
        promptService.printInfo("Usuários disponíveis:\n");

        var users = ctx.userUseCase().getAllUsers();
        if (users.isEmpty()) {
            promptService.printWarning("Nenhum usuário encontrado.");
        } else {
            promptService.printInfo(String.format("%-20s | %-30s", "Nome", "Email"));
            promptService.printInfo("--------------------------------------------------");
            for (var u : users) {
                promptService.printInfo(String.format("%-20s | %-30s", u.getName(), u.getEmail()));
            }
        }
        promptService.printInfo("");

        promptService.printMenuOptions(
            "Criar Novo Usuário",
            "Gerenciar Contas do Usuário"
        );
    }

    /**
     * Retorna a lista de opções disponíveis no menu.
     *
     * @return Lista com as opções do menu
     */
    @Override
    public List<String> getOptions() {
        return new ArrayList<>(transitions.keySet());
    }

    /**
     * Processa a entrada do usuário e realiza a transição para o menu correspondente.
     *
     * @param promptService Serviço para ler entrada do usuário
     * @return O próximo menu a ser exibido, ou null se a opção 0 for selecionada
     */
    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha uma opção: ").trim();
        Supplier<BaseMenu> transition = transitions.get(option);

        if (transition != null) {
            return transition.get();
        } else {
            promptService.printError("Opção inválida. Tente novamente.");
            return this; // Retorna ao mesmo menu em caso de opção inválida
        }
    }

}
