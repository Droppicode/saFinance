package com.safinance.view;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Function;

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

    /**
     * Registra uma transição de estado para ser utilizada em um Map de comandos.
     */
    default void registerTransition(String command, Supplier<BaseMenu> stateSupplier, Map<String, Supplier<BaseMenu>> transitions) {
        transitions.put(command, stateSupplier);
    }

    /**
     * Registra uma transição de estado que necessita do PromptService imediatamente.
     */
    default void registerTransition(String command, Function<PromptService, BaseMenu> stateFunction, Map<String, Function<PromptService, BaseMenu>> transitions) {
        transitions.put(command, stateFunction);
    }
}

