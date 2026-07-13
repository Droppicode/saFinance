package com.safinance.view.actions;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

public class CreateWalletAccountAction implements BaseMenu {

    private final User accountOwner;
    private final AccountUseCase accountUseCase;
    private final Supplier<BaseMenu> onComplete;

    public CreateWalletAccountAction(User accountOwner, AccountUseCase accountUseCase, Supplier<BaseMenu> onComplete) {
        this.accountOwner = accountOwner;
        this.accountUseCase = accountUseCase;
        this.onComplete = onComplete;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Criar Conta Carteira");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String name = promptService.readString("Nome da Conta Carteira: ").trim();
        
        try {
            accountUseCase.createWalletAccount(accountOwner, 0.0, null, name);
            promptService.printSuccess("Conta carteira '" + name + "' criada com sucesso!");
        } catch (Exception e) {
            promptService.printError("Erro ao criar conta carteira: " + e.getMessage());
        }
        
        promptService.readString("Pressione Enter para voltar ao menu de contas.");
        return onComplete.get();
    }
}
