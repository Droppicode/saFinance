package com.safinance.view;

import com.safinance.core.domain.Role;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.UserUseCase;

import java.util.Collections;
import java.util.List;

/**
 * Menu de Registro (Criação de nova conta de usuário).
 */
public class RegisterMenu implements BaseMenu {

    private final AuthUseCase authUseCase;
    private final UserUseCase userUseCase;
    private final AccountUseCase accountUseCase;

    public RegisterMenu(AuthUseCase authUseCase, UserUseCase userUseCase, AccountUseCase accountUseCase) {
        this.authUseCase = authUseCase;
        this.userUseCase = userUseCase;
        this.accountUseCase = accountUseCase;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Criar Nova Conta");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String name = promptService.readString("Nome completo: ");
        String email = promptService.readString("Email: ");
        String password = promptService.readString("Senha: ");

        try {
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Todos os campos são obrigatórios.");
            }
            userUseCase.createUser(name, email, password, Role.REGULAR);
            promptService.printSuccess("Conta criada com sucesso! Faça login para continuar.");
        } catch (Exception e) {
            promptService.printError("Erro ao criar conta: " + e.getMessage());
        }
        
        promptService.readString("Pressione Enter para retornar ao menu principal.");
        return new WelcomeMenu(authUseCase, userUseCase, accountUseCase);
    }
}
