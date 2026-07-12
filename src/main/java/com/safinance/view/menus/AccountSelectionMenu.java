package com.safinance.view.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.safinance.core.domain.Account;
import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.actions.ApplyYieldAction;

/**
 * Menu para seleção de contas de poupança, permitindo que o usuário escolha uma conta específica 
 * para aplicar rendimentos.
 */
public class AccountSelectionMenu implements BaseMenu {
    
    private final User user;    
    private final User accountOwner;
    private final UserUseCase userUseCase;
    private final BankUseCase bankUseCase;
    private final AccountUseCase accountUseCase;
    private final TransactionUseCase transactionUseCase;

    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    /**
     *  Construtor da classe.
     * @param user O usuário logado.
     * @param accountOwner O usuário cujas contas estão sendo gerenciadas.
     * @param userUseCase A instância do caso de uso de usuários.
     * @param bankUseCase A instância do caso de uso de bancos.
     * @param accountUseCase A instância do caso de uso de contas.
     */
    public AccountSelectionMenu(User user, User accountOwner, UserUseCase userUseCase, BankUseCase bankUseCase, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.accountOwner = accountOwner;
        this.userUseCase = userUseCase;
        this.bankUseCase = bankUseCase;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
    }

    /**
     * Renderiza o cabeçalho do menu.
     * @param promptService A instância do serviço de prompt.
     */
    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Selecionar Conta de Poupança");
        promptService.printInfo("");
        
        transitions.put("0", () -> new ManageAccountsMenu(user, accountOwner, userUseCase, bankUseCase, accountUseCase, transactionUseCase));

        List<Account> accounts = accountUseCase.listUserAccounts(accountOwner);
        List<SavingsAccount> savingsAccounts = accounts.stream()
                .filter(account -> account instanceof SavingsAccount)
                .map(account -> (SavingsAccount) account)
                .toList();
        if (savingsAccounts.isEmpty()) {
            promptService.printWarning("Nenhuma conta de poupança encontrada.\n");
            return;
        }

        int index = 1;
        for (SavingsAccount sa : savingsAccounts) {
            promptService.printInfo(index + ". (Saldo: R$ " + sa.getBalance() + ")");
            final int chosenIndex = index;
            transitions.put(String.valueOf(chosenIndex), () -> new ApplyYieldAction(user, accountOwner, sa, userUseCase, bankUseCase, accountUseCase, transactionUseCase));
            index++;
        }

        promptService.printInfo("0. Voltar\n");
    }

    @Override
    public List<String> getOptions() {
        return new ArrayList<>(transitions.keySet());
    }

    /**
     * Manipula a entrada do usuário.
     * @param promptService A instância do serviço de prompt.
     * @return O menu correspondente à opção escolhida.
     */
    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha a conta de poupança pelo índice ou '0' para voltar: ").trim();
        Supplier<BaseMenu> transition = transitions.get(option);

        if (transition != null) {
            return transition.get();
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }
    
}
