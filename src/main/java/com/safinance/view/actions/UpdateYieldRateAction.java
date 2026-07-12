package com.safinance.view.actions;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.ManageBanksMenu;

public class UpdateYieldRateAction implements BaseMenu {
    
    private final User user;
    private final BankUseCase bankUseCase;
    private final UserUseCase userUseCase;
    private final AccountUseCase accountUseCase;

    public UpdateYieldRateAction(User user, BankUseCase bankUseCase, UserUseCase userUseCase, AccountUseCase accountUseCase) {
        this.user = user;
        this.bankUseCase = bankUseCase;
        this.userUseCase = userUseCase;
        this.accountUseCase = accountUseCase;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Atualizar Taxa de Rendimento");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        try {
            // Lê o mês no formato YYYY-MM
            YearMonth month = promptService.readYearMonth("Mês (YYYY-MM): ");
            double newRate = promptService.readDouble("Nova taxa de rendimento (%%): ");
            bankUseCase.updateYieldRate(month, newRate);
            promptService.printInfo("Taxa de rendimento atualizada com sucesso!");
        } catch (Exception e) {
            promptService.printError("Erro ao atualizar taxa de rendimento." + e.getMessage());
            return this;
        }

        promptService.readString("Pressione Enter para retornar.");
        return new ManageBanksMenu(user, bankUseCase, userUseCase, accountUseCase); // Retorna ao menu anterior
    }
    
}
