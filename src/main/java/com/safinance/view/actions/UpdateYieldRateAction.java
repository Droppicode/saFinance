package com.safinance.view.actions;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.safinance.core.usecases.BankUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

public class UpdateYieldRateAction implements BaseMenu {
    
    private final BankUseCase bankUseCase;
    private final Supplier<BaseMenu> onComplete;

    public UpdateYieldRateAction(BankUseCase bankUseCase, Supplier<BaseMenu> onComplete) {
        this.bankUseCase = bankUseCase;
        this.onComplete = onComplete;
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
            YearMonth month = promptService.readYearMonth("Mês (YYYY-MM): ");
            if (month == null) {
                promptService.printError("Mês inválido.");
                promptService.readString("Pressione Enter para retornar.");
                return onComplete.get();
            }

            Double newRate = promptService.readDouble("Nova taxa de rendimento (%): ");
            if (newRate == null) {
                promptService.printError("Taxa inválida. Digite um número válido.");
                promptService.readString("Pressione Enter para retornar.");
                return onComplete.get();
            }

            bankUseCase.updateYieldRate(month, newRate / 100.0);
            promptService.printSuccess("Taxa de rendimento atualizada com sucesso!");
        } catch (Exception e) {
            promptService.printError("Erro ao atualizar taxa de rendimento: " + e.getMessage());
        }

        promptService.readString("Pressione Enter para retornar.");
        return onComplete.get();
    }
    
}
