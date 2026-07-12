package com.safinance.view.actions;


import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

public class CreateSavingsAccountAction implements BaseMenu {

    private final User accountOwner;
    private final AccountUseCase accountUseCase;
    private final Supplier<BaseMenu> onComplete;

    public CreateSavingsAccountAction(User accountOwner, AccountUseCase accountUseCase, Supplier<BaseMenu> onComplete) {
        this.accountOwner = accountOwner;
        this.accountUseCase = accountUseCase;
        this.onComplete = onComplete;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Criar Conta Poupança");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String name = promptService.readString("Nome da Conta Poupança: ").trim();
        
        try {
            accountUseCase.createSavingsAccount(accountOwner, 0.0, name);
            promptService.printSuccess("Conta poupança '" + name + "' criada com sucesso!");
        } catch (Exception e) {
            promptService.printError("Erro ao criar conta poupança: " + e.getMessage());
        }
        
        promptService.readString("Pressione Enter para voltar ao menu de contas.");
        return onComplete.get();
    }
}
