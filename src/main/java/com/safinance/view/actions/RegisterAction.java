package com.safinance.view.actions;

import java.util.Collections;
import java.util.List;

import com.safinance.core.domain.Role;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.ManageUsersMenu;
import com.safinance.view.menus.WelcomeMenu;

/**
 * Menu de Registro (Criação de nova conta de usuário).
 */
public class RegisterAction implements BaseMenu {

    private final User user;
    private final AuthUseCase authUseCase;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    public RegisterAction(User user, AuthUseCase authUseCase, UserUseCase userUseCase, BankUseCase bankUseCase, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.authUseCase = authUseCase;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
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
        if (user != null && user.getRole() != Role.ADMIN) {
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
            if (user != null && user.getRole() != Role.ADMIN) {
                message += " Faça login para continuar.";
            }
            promptService.printSuccess(message);
        } catch (Exception e) {
            promptService.printError("Erro ao criar conta: " + e.getMessage());
        }
        
        promptService.readString("Pressione Enter para retornar.");
        if (user != null && user.getRole() == Role.ADMIN) {
            return new ManageUsersMenu(user, bankUseCase, userUseCase, accountUseCase, transactionUseCase);
        }
        return new WelcomeMenu(authUseCase, userUseCase, bankUseCase, accountUseCase, transactionUseCase);
    }
}