package com.safinance.view.actions;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.ManageAccountsMenu;

/**
 * Ação para aplicar rendimento em uma conta de poupança específica.
 */
public class ApplyYieldAction implements BaseMenu {

    private final User user;
    private final User accountOwner;
    private final SavingsAccount sa;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;

    /**
     * Construtor da classe.
     * @param user O usuário logado.
     * @param accountOwner O usuário cujas contas estão sendo gerenciadas.
     * @param sa A conta de poupança na qual aplicar o rendimento.
     * @param userUseCase A instância do caso de uso de usuários.
     * @param bankUseCase A instância do caso de uso de bancos.
     * @param accountUseCase A instância do caso de uso de contas.
     */
    public ApplyYieldAction(User user, User accountOwner, SavingsAccount sa, UserUseCase userUseCase, BankUseCase bankUseCase, AccountUseCase accountUseCase) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.sa = sa;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;
    }

    /**
     * Renderiza o cabeçalho do menu.
     * @param promptService A instância do serviço de prompt.
     */
    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Aplicar Rendimento");
    }

    /**
     * Retorna as opções disponíveis no menu.
     * @return Uma lista com as opções disponíveis.
     */
    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    /**
     * Manipula a entrada do usuário.
     * @param promptService A instância do serviço de prompt.
     * @return O menu correspondente à opção escolhida.
     */
    @Override
    public BaseMenu handleInput(PromptService promptService) {
        try {
            YearMonth month =promptService.readYearMonth("Digite o ano e mês para aplicar o rendimento (formato: YYYY-MM): ");
            accountUseCase.applyYield(this.sa, month);
            promptService.printSuccess("Rendimento aplicado com sucesso.");
        } catch (Exception e) {
            promptService.printError("Erro ao aplicar rendimento.");
        }
        promptService.readString("Pressione Enter para retornar.");
        return new ManageAccountsMenu(user, accountOwner, userUseCase, bankUseCase, accountUseCase);
    }
}
