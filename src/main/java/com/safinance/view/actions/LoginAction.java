package com.safinance.view.actions;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

/**
 * Menu de Login.
 */
public class LoginAction implements BaseMenu {

    private final AuthUseCase authUseCase;
    private final Function<User, BaseMenu> onSuccess;
    private final Supplier<BaseMenu> onFail;

    public LoginAction(AuthUseCase authUseCase, Function<User, BaseMenu> onSuccess, Supplier<BaseMenu> onFail) {
        this.authUseCase = authUseCase;
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Login");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String email = promptService.readString("Email: ");
        String password = promptService.readString("Senha: ");

        try {
            if (email.equalsIgnoreCase("teste")) {
                email = "admin@safinance.com";
                password = "123456";
                promptService.printInfo("Atalho de teste utilizado. Logando como Admin...");
            }

            User loggedIn = authUseCase.login(email, password);
            promptService.printSuccess("Login realizado com sucesso! Bem-vindo(a), " + loggedIn.getName() + ".");
            promptService.readString("Pressione Enter para continuar.");

            return onSuccess.apply(loggedIn);
        } catch (Exception e) {
            promptService.printError("Credenciais inválidas: " + e.getMessage());
            promptService.readString("Pressione Enter para voltar ao menu inicial.");
            return onFail.get();
        }
    }
}