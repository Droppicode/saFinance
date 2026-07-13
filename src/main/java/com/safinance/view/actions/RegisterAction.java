package com.safinance.view.actions;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.safinance.core.domain.Role;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

/**
 * Menu de Registro (Criação de nova conta de usuário).
 */
public class RegisterAction implements BaseMenu {

    private final User user;
    private final UserUseCase userUseCase;
    private final Supplier<BaseMenu> onComplete;

    public RegisterAction(User user, UserUseCase userUseCase, Supplier<BaseMenu> onComplete) {
        this.user = user;
        this.userUseCase = userUseCase;
        this.onComplete = onComplete;
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
        Role role = Role.REGULAR; // Default role
        
        // BUG FIX: Somente admins podem criar outros admins ou definir papéis.
        if (user != null && user.getRole() == Role.ADMIN) {
            String roleInput = promptService.readString("Tipo de usuário (admin/regular): ").toUpperCase();
            if (roleInput.equals("ADMIN")) {
                role = Role.ADMIN;
            }
        }

        try {
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Todos os campos são obrigatórios.");
            }
            
            userUseCase.createUser(name, email, password, role);
            
            String message = "Conta criada com sucesso!";
            if (user == null || user.getRole() != Role.ADMIN) {
                message += " Faça login para continuar.";
            }
            promptService.printSuccess(message);
        } catch (Exception e) {
            promptService.printError("Erro ao criar conta: " + e.getMessage());
        }
        
        promptService.readString("Pressione Enter para retornar.");
        return onComplete.get();
    }
}