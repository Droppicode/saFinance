package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.core.domain.Role;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.InvestmentUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.actions.RegisterAction;

/**
 * Menu para gerenciar usuários do sistema.
 */
public class ManageUsersMenu implements BaseMenu {

    private final User user;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;
    private final InvestmentUseCase investmentUseCase;
    private final TransactionUseCase transactionUseCase;
    
    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    /**
     * Construtor do menu de gerenciamento de usuários.
     *
     * @param user O usuário logado
     * @param bankUseCase O caso de uso para operações com bancos
     * @param userUseCase O caso de uso para operações com usuários
     * @param accountUseCase O caso de uso para operações com contas
     */
    public ManageUsersMenu(User user, BankUseCase bankUseCase, UserUseCase userUseCase, AccountUseCase accountUseCase, InvestmentUseCase investmentUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.bankUseCase = bankUseCase;
        this.userUseCase = userUseCase;
        this.accountUseCase = accountUseCase;
        this.investmentUseCase = investmentUseCase;
        this.transactionUseCase = transactionUseCase;

        // Registra as opções do menu e as transições correspondentes.
        registerTransition("1", () -> new RegisterAction(user, null, userUseCase, null, accountUseCase, investmentUseCase, transactionUseCase), transitions);
        registerTransition("2", () -> new UserSelectionMenu(user, bankUseCase, userUseCase, accountUseCase, investmentUseCase, transactionUseCase), transitions);
        registerTransition("0", () -> new AdminMenu(user, userUseCase, bankUseCase, accountUseCase, investmentUseCase, transactionUseCase), transitions);

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

        var users = userUseCase.getAllUsers();
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
