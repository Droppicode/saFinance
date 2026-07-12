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
import com.safinance.view.actions.UpdateOperationTaxAction;
import com.safinance.view.actions.UpdateYieldRateAction;

/**
 * Menu para gerenciar bancos, permitindo que administradores configurem taxas de rendimento e taxas de operação.
 */
public class ManageBanksMenu implements BaseMenu {

    private final User user;
    private final BankUseCase bankUseCase;
    private final UserUseCase userUseCase;
    private final AccountUseCase accountUseCase;
    private final InvestmentUseCase investmentUseCase;
    private final TransactionUseCase transactionUseCase;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    /**
     * Construtor da classe ManageBanksMenu.
     * @param user O usuário logado.
     * @param bankUseCase A instância do caso de uso de bancos.
     * @param userUseCase A instância do caso de uso de usuários.
     * @param accountUseCase A instância do caso de uso de contas.
     */
    public ManageBanksMenu(User user, BankUseCase bankUseCase, UserUseCase userUseCase, AccountUseCase accountUseCase, InvestmentUseCase investmentUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.bankUseCase = bankUseCase;
        this.userUseCase = userUseCase;
        this.accountUseCase = accountUseCase;
        this.investmentUseCase = investmentUseCase;
        this.transactionUseCase = transactionUseCase;

        // Registra transições do menu para ações específicas.
        registerTransition("1", () -> new UpdateYieldRateAction(user, bankUseCase, userUseCase, accountUseCase, investmentUseCase, transactionUseCase), transitions);
        registerTransition("2", () -> new UpdateOperationTaxAction(user, bankUseCase, userUseCase, accountUseCase, investmentUseCase, transactionUseCase), transitions);
        registerTransition("0", () -> null, transitions);
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
            return new AdminMenu(user, userUseCase, bankUseCase, accountUseCase, investmentUseCase, transactionUseCase);
        }
    }
}
