package com.safinance.view;

import java.util.List;

/**
 * Interface que representa um menu base (Estado) para a aplicação.
 */
public interface BaseMenu {
    
    /**
     * Renderiza o cabeçalho e informações estáticas da tela.
     */
    void renderHeader(PromptService promptService);

    /**
     * Processa a entrada do usuário e retorna a próxima tela (Estado).
     * @return O próximo BaseMenu a ser exibido, ou null para encerrar/voltar.
     */
    BaseMenu handleInput(PromptService promptService);

    /**
     * Retorna as opções disponíveis para autocompletar.
     */
    List<String> getOptions();
}

