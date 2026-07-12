package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.domain.User;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;
import com.safinance.view.actions.ApplyYieldAction;

/**
 * Menu para seleção de contas de poupança, permitindo que o usuário escolha uma conta específica 
 * para aplicar rendimentos.
 */
public class AccountSelectionMenu implements BaseMenu {
    
    private final User user;    
    private final User accountOwner;
    private final MenuContext ctx;

    /**
     *  Construtor da classe.
     * @param user O usuário logado.
     * @param accountOwner O usuário cujas contas estão sendo gerenciadas.
     * @param ctx O contexto com todas as dependências de caso de uso.
     */
    public AccountSelectionMenu(User user, User accountOwner, MenuContext ctx) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.ctx = ctx;
    }

    /**
     * Renderiza o cabeçalho do menu.
     * @param promptService A instância do serviço de prompt.
     */
    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Selecionar Conta de Poupança");
        promptService.printInfo("");

        List<SavingsAccount> savingsAccounts = ctx.accountUseCase().listUserAccountsOfType(accountOwner, SavingsAccount.class);
        if (savingsAccounts.isEmpty()) {
            promptService.printWarning("Nenhuma conta de poupança encontrada.\n");
            return;
        }

        int index = 1;
        for (SavingsAccount sa : savingsAccounts) {
            promptService.printInfo(index + ". (Saldo: R$ " + sa.getBalance() + ")");
            index++;
        }

        promptService.printInfo("0. Voltar\n");
    }

    @Override
    public List<String> getOptions() {
        return List.of("0");
    }

    /**
     * Manipula a entrada do usuário.
     * As transições dinâmicas são construídas aqui (não no renderHeader) para evitar side effects no render.
     * @param promptService A instância do serviço de prompt.
     * @return O menu correspondente à opção escolhida.
     */
    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha a conta de poupança pelo índice ou '0' para voltar: ").trim();

        if (option.equals("0")) {
            return new ManageAccountsMenu(user, accountOwner, ctx);
        }

        List<SavingsAccount> savingsAccounts = ctx.accountUseCase().listUserAccountsOfType(accountOwner, SavingsAccount.class);

        try {
            int index = Integer.parseInt(option) - 1;
            if (index >= 0 && index < savingsAccounts.size()) {
                SavingsAccount selected = savingsAccounts.get(index);
                return new ApplyYieldAction(ctx.accountUseCase(), selected, () -> new ManageAccountsMenu(user, accountOwner, ctx));
            }
        } catch (NumberFormatException ignored) {}

        promptService.printError("Opção inválida.");
        promptService.readString("Pressione Enter para tentar novamente.");
        return this;
    }
    
}
