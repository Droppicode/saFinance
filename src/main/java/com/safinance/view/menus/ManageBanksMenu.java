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
import com.safinance.view.actions.UpdateOperationTaxAction;
import com.safinance.view.actions.UpdateYieldRateAction;

/**
 * Menu para gerenciar bancos, permitindo que administradores configurem taxas de rendimento e taxas de operação.
 */
public class ManageBanksMenu implements BaseMenu {

    private final User user;
    private final MenuContext ctx;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    /**
     * Construtor da classe ManageBanksMenu.
     * @param user O usuário logado.
     * @param ctx O contexto com todas as dependências de caso de uso.
     */
    public ManageBanksMenu(User user, MenuContext ctx) {
        this.user = user;
        this.ctx = ctx;

        // Registra transições do menu para ações específicas.
        registerTransition("1", () -> new UpdateYieldRateAction(ctx.bankUseCase(), () -> this), transitions);
        registerTransition("2", () -> new UpdateOperationTaxAction(ctx.bankUseCase(), () -> this), transitions);
        registerTransition("0", () -> new AdminMenu(user, ctx), transitions);
    }

    @Override
    public void renderHeader(PromptService promptService) {
        // Verifica se o usuário tem permissão de administrador.
        if (user == null || user.getRole() != Role.ADMIN) {
            promptService.printError("Acesso negado. Apenas administradores podem gerenciar bancos.");
            return;
        }

        promptService.printHeader("Gerenciar Bancos");
        promptService.printInfo("Configurações de cálculo de juros e taxas");

        // Exibe as opções disponíveis para o administrador.
        promptService.printMenuOptions(
            "Configurar juros",
            "Configurar taxas"
        );
    }   

    @Override
    public List<String> getOptions() {
        // Retorna as opções disponíveis no menu.
        return new ArrayList<>(transitions.keySet());
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        // Lê a opção informada pelo usuário.
        String option = promptService.readString("> Escolha uma opção: ").trim();
        Supplier<BaseMenu> transition = transitions.get(option);
        if (transition != null) {
            if (option.equals("0")) {
                promptService.printSuccess("Retornando ao menu anterior.");
            }
            return transition.get();
        } else {
            promptService.printError("Opção inválida. Tente novamente.");
            return this;
        }
    }
}
