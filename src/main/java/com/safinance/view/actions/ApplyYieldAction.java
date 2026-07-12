package com.safinance.view.actions;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

/**
 * Ação para aplicar rendimento em uma conta de poupança específica.
 */
public class ApplyYieldAction implements BaseMenu {

    private final AccountUseCase accountUseCase;
    private final SavingsAccount sa;
    private final Supplier<BaseMenu> onComplete;

    public ApplyYieldAction(AccountUseCase accountUseCase, SavingsAccount sa, Supplier<BaseMenu> onComplete) {
        this.accountUseCase = accountUseCase;
        this.sa = sa;
        this.onComplete = onComplete;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Aplicar Rendimento");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        try {
            YearMonth month = promptService.readYearMonth("Digite o ano e mês para aplicar o rendimento (formato: YYYY-MM): ");
            if (month == null) {
                promptService.printError("Formato de data inválido.");
                promptService.readString("Pressione Enter para retornar.");
                return onComplete.get();
            }

            accountUseCase.applyYield(this.sa, month);
            promptService.printSuccess("Rendimento aplicado com sucesso.");
        } catch (Exception e) {
            promptService.printError("Erro ao aplicar rendimento.");
        }
        promptService.readString("Pressione Enter para retornar.");
        return onComplete.get();
    }
}
