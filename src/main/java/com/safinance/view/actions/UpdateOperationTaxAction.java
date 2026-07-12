package com.safinance.view.actions;

import java.util.Collections;
import java.util.List;

import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;
import com.safinance.view.menus.ManageBanksMenu;

/**
 * Ação para atualizar a taxa de operação de um banco.
 */
public class UpdateOperationTaxAction implements BaseMenu {
    
    private final User user;
    private final BankUseCase bankUseCase;
    private final UserUseCase userUseCase;
    private final AccountUseCase accountUseCase;
        private final TransactionUseCase transactionUseCase;

    /**
     * Construtor que inicializa a ação com as dependências necessárias
     * @param user Usuário logado
     * @param bankUseCase Use case bancário
     * @param userUseCase Use case de usuário
     * @param accountUseCase Use case de conta
     */
    public UpdateOperationTaxAction(User user, BankUseCase bankUseCase, UserUseCase userUseCase, AccountUseCase accountUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.bankUseCase = bankUseCase;
        this.userUseCase = userUseCase;
        this.accountUseCase = accountUseCase;
        this.transactionUseCase = transactionUseCase;
    }

    /**
     * Exibe o cabeçalho do menu de atualização de taxa de operação
     * @param promptService Serviço de prompt para exibição
     */
    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Atualizar Taxa de Operação");
    }

    /**
     * Retorna a lista de opções do menu (vazio para esta ação)
     * @return Lista vazia de opções
     */
    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    /**
     * Processa a entrada do usuário para atualizar a taxa de operação
     * @param promptService Serviço de prompt para entrada e saída
     * @return Menu de gerenciamento de bancos ou retorna a si mesmo em caso de erro
     */
    @Override
    public BaseMenu handleInput(PromptService promptService) {
        try {
            String operationType = promptService.readString("Tipo de operação: ");
            double newRate = promptService.readDouble("Nova taxa de operação (%%): ");
            bankUseCase.updateOperationTax(operationType, newRate);
            promptService.printInfo("Taxa de operação atualizada com sucesso!");
        } catch (Exception e) {
            promptService.printError("Erro ao atualizar taxa de operação." + e.getMessage());
            return this;
        }

        // Aguarda o usuário pressionar Enter para retornar
        promptService.readString("Pressione Enter para retornar.");
        // Retorna ao menu anterior
        return new ManageBanksMenu(user, bankUseCase, userUseCase, accountUseCase, transactionUseCase);
    }
    
}
