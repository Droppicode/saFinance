package com.safinance.view.actions;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.safinance.core.usecases.BankUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

/**
 * Ação para atualizar a taxa de operação de um banco.
 */
public class UpdateOperationTaxAction implements BaseMenu {
    
    private final BankUseCase bankUseCase;
    private final Supplier<BaseMenu> onComplete;

    public UpdateOperationTaxAction(BankUseCase bankUseCase, Supplier<BaseMenu> onComplete) {
        this.bankUseCase = bankUseCase;
        this.onComplete = onComplete;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Atualizar Taxa de Operação");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        try {
            String operationType = promptService.readString("Tipo de operação: ");
            
            Double newRate = promptService.readDouble("Nova taxa de operação (%): ");
            if (newRate == null) {
                promptService.printError("Taxa inválida. Digite um número válido.");
                promptService.readString("Pressione Enter para retornar.");
                return onComplete.get();
            }

            bankUseCase.updateOperationTax(operationType, newRate / 100.0);
            promptService.printSuccess("Taxa de operação atualizada com sucesso!");
        } catch (Exception e) {
            promptService.printError("Erro ao atualizar taxa de operação: " + e.getMessage());
        }

        promptService.readString("Pressione Enter para retornar.");
        return onComplete.get();
    }
    
}
