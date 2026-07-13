package com.safinance.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Base abstract menu that implements the Template Method and Command patterns.
 * It manages options display and input handling to avoid duplication across menus.
 */
public abstract class AbstractMenu implements BaseMenu {

    private final Map<String, MenuCommand> commands = new LinkedHashMap<>();

    protected static class MenuCommand {
        private final String label;
        private final Function<PromptService, BaseMenu> action;

        public MenuCommand(String label, Function<PromptService, BaseMenu> action) {
            this.label = label;
            this.action = action;
        }

        public String getLabel() {
            return label;
        }

        public Function<PromptService, BaseMenu> getAction() {
            return action;
        }
    }

    /**
     * Registers a command for this menu.
     * @param key the option key (e.g. "1")
     * @param label the display label
     * @param action the action to execute
     */
    protected void registerCommand(String key, String label, Function<PromptService, BaseMenu> action) {
        commands.put(key, new MenuCommand(label, action));
    }

    @Override
    public List<String> getOptions() {
        return new ArrayList<>(commands.keySet());
    }

    /**
     * Subclasses must implement this to print their specific header.
     * The options will be printed automatically by the AbstractMenu.
     */
    protected abstract void printHeader(PromptService promptService);

    @Override
    public void renderHeader(PromptService promptService) {
        printHeader(promptService);
        if (!commands.isEmpty()) {
            for (Map.Entry<String, MenuCommand> entry : commands.entrySet()) {
                promptService.printInfo(entry.getKey() + " - " + entry.getValue().getLabel());
            }
        }
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha uma opção: ").trim();
        return processOption(promptService, option);
    }

    protected BaseMenu processOption(PromptService promptService, String option) {
        MenuCommand command = commands.get(option);

        if (command != null) {
            return command.getAction().apply(promptService);
        } else {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }
    }
}
