package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

/**
 * Menu principal para usuários administradores.
 */
public class AdminMenu implements BaseMenu {

    private final User user;    
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    /**
     * Construtor da classe.
     * @param user O usuário logado.
     * @param accountUseCase A instância do caso de uso de contas.
     */
    public AdminMenu(User user, UserUseCase userUseCase, BankUseCase bankUseCase, AccountUseCase accountUseCase) {
        this.user = user;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;

        registerTransition("1", () -> new ManageUsersMenu(user, bankUseCase, userUseCase, accountUseCase), transitions);
        registerTransition("2", () -> new ManageAccountsMenu(user, user, userUseCase, bankUseCase, accountUseCase), transitions);
        registerTransition("3", () -> new ManageBanksMenu(user, bankUseCase, userUseCase, accountUseCase), transitions);
        registerTransition("0", () -> null, transitions);
    }

    /**
     * Exibe o menu do usuário.
     */
    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Menu do Usuário");
        promptService.printInfo("Bem-vindo, " + user.getName() + "!");
        promptService.printMenuOptions(
            "Gerenciar usuários",
            "Gerenciar contas",
            "Gerenciar bancos",
            "Extrato financeiro",
            "Investimentos"
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
                promptService.printSuccess("Encerrando sessão. Até logo!");
            }
            return transition.get();
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }   
}
