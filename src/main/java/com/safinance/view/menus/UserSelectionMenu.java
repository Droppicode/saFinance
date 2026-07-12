package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

/**
 * Menu para seleção de usuários, permitindo que o administrador escolha um usuário específico para gerenciar suas contas.
 */
public class UserSelectionMenu implements BaseMenu {

    private final User user;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    /**
     * Construtor da classe.
     * @param user O usuário logado (administrador).
     * @param bankUseCase A instância do caso de uso de bancos.
     * @param userUseCase A instância do caso de uso de usuários.
     * @param accountUseCase A instância do caso de uso de contas.
     * @param transactionUseCase A instância do caso de uso de transações.
     */
    public UserSelectionMenu(User user, BankUseCase bankUseCase, UserUseCase userUseCase, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.bankUseCase = bankUseCase;
        this.userUseCase = userUseCase;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;   
    }

    /**
     * Renderiza o cabeçalho do menu.
     * @param promptService A instância do serviço de prompt.
     */
    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Seleção de Usuário");
        promptService.printInfo("");

        transitions.put("0", () -> new ManageUsersMenu(user, bankUseCase, userUseCase, accountUseCase, transactionUseCase));

        var users = userUseCase.getAllUsers();
        if (users.isEmpty()) {
            promptService.printWarning("Nenhum usuário encontrado.\n");
            return;
        }

        int index = 1;
        for (User u : users) {
            promptService.printInfo(index + ". " + u.getName() + " (" + u.getEmail() + ")");
            final int chosenIndex = index;
            transitions.put(String.valueOf(chosenIndex), () -> new ManageAccountsMenu(user, u, userUseCase, bankUseCase, accountUseCase, transactionUseCase));
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
     * @param promptService A instância do serviço de prompt.
     * @return O menu correspondente à opção escolhida.
     */
    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha o usuário pelo índice ou digite '0' para voltar: ").trim();
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
