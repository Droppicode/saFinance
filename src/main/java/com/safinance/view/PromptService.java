package com.safinance.view;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;

/**
 * Encapsulates all terminal I/O interactions for the interactive CLI.
 *
 * <p>This class acts as the single boundary between the View layer and the
 * JLine library, isolating prompt rendering, user input collection, and
 * styled output behind a simple API. All {@code InteractiveCLIHandler}
 * implementations receive a {@code PromptService} instead of raw
 * {@code Terminal} or {@code LineReader} references.</p>
 *
 * <p>The service supports dynamic autocompletion by swapping the active
 * {@link Completer} before each prompt that requires contextual suggestions.</p>
 */
public class PromptService {
    private final LineReader reader;
    private final Terminal terminal;
    private Completer activeCompleter;

    /**
     * Constructs a PromptService bound to the given JLine reader and terminal.
     *
     * @param reader   the line reader used to collect user input
     * @param terminal the terminal used for styled output
     */
    public PromptService(LineReader reader, Terminal terminal) {
        this.reader = reader;
        this.terminal = terminal;
    }

    /**
     * Reads a plain text input from the user.
     *
     * @param promptText the prompt label displayed before the cursor
     * @return the trimmed user input
     */
    public String readString(String promptText) {
        return reader.readLine(promptText).trim();
    }


    /**
     * Reads a double value from the user, returning null if the input is not a valid number.
     * 
     * @param promptText the prompt label displayed before the cursor
     * @return the parsed double value or null if invalid
     */
    public Double readDouble(String promptText) {
        String input = readString(promptText).replace(',', '.');
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    /**
     * Reads a YearMonth value from the user, returning null if the input is not a valid YearMonth.
     * @param promptText the prompt label displayed before the cursor
     * @return the parsed YearMonth value or null if invalid
     */
    public java.time.YearMonth readYearMonth(String promptText) {
        String input = readString(promptText);
        try {
            return java.time.YearMonth.parse(input);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Escreve {@code text} (pode ser multilinha) de forma segura mesmo com um
     * {@code readLine} ativo: usa {@link LineReader#printAbove(String)}, que
     * imprime o texto acima do prompt e redesenha o prompt (com o que o usuário
     * já digitou) logo abaixo. Escrever direto no {@code terminal.writer()}
     * durante um readLine embaralha a tela, porque o JLine não sabe que o
     * cursor se moveu. Fora de um readLine (ou em terminal burro/console da
     * IDE), comporta-se como um println normal.
     *
     * @param text o texto a imprimir (linhas separadas por {@code \n})
     */
    public void printLive(String text) {
        clearScreen();
        reader.printAbove(text);
    }

    /**
     * Lê uma linha do usuário enquanto executa {@code onTick} periodicamente em
     * segundo plano (ex: avançar e reimprimir as cotações a cada N ms).
     *
     * <p>O agendador roda APENAS durante esta leitura e é encerrado assim que o
     * usuário confirma a entrada. Assim, o restante do fluxo (que lê os preços na
     * compra/venda) nunca concorre com o refresh.</p>
     *
     * @param promptText     rótulo do prompt
     * @param intervalMillis intervalo entre execuções de {@code onTick}, em ms
     * @param onTick         ação periódica (tipicamente avança o mercado e imprime via {@link #printLive})
     * @return a linha digitada, sem espaços nas pontas
     */
    public String readStringWithRefresh(String promptText, long intervalMillis, Runnable onTick) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "market-refresh");
            thread.setDaemon(true);
            return thread;
        });
        try {
            scheduler.scheduleAtFixedRate(onTick, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
            return reader.readLine(promptText).trim();
        } finally {
            scheduler.shutdownNow();
        }
    }

    /**
     * Reads user input while temporarily enabling autocompletion
     * with the supplied options.
     *
     * <p>The previous completer is restored after the prompt completes,
     * regardless of whether an exception occurs.</p>
     *
     * @param promptText the prompt label displayed before the cursor
     * @param options    the list of valid options offered via Tab completion
     * @return the trimmed user input
     */
    public String readWithOptions(String promptText, List<String> options) {
        Completer previous = this.activeCompleter;
        try {
            this.activeCompleter = new StringsCompleter(options);
            return reader.readLine(promptText).trim();
        } finally {
            this.activeCompleter = previous;
        }
    }

    /**
     * Prints a success message styled in green with a check-mark prefix.
     *
     * @param message the success message to display
     */
    public void printSuccess(String message) {
        terminal.writer().println("\u001B[32m✔ " + message + "\u001B[0m");
    }

    /**
     * Prints a warning message styled in yellow with a warning prefix.
     *
     * @param message the warning message to display
     */
    public void printWarning(String message) {
        terminal.writer().println("\u001B[33m⚠ " + message + "\u001B[0m");
    }

    /**
     * Prints an error message styled in red with a cross-mark prefix.
     *
     * @param message the error message to display
     */
    public void printError(String message) {
        terminal.writer().println("\u001B[31m❌ " + message + "\u001B[0m");
    }

    /**
     * Prints an informational message with no special styling.
     *
     * @param message the message to display
     */
    public void printInfo(String message) {
        terminal.writer().println(message);
    }

    /**
     * Prints a numbered list of items with a title header.
     *
     * @param title the label printed above the list
     * @param items the items to display as {@code [1] item, [2] item, ...}
     */
    public void printNumberedList(String title, List<String> items) {
        printInfo(title);
        for (int i = 0; i < items.size(); i++) {
            printInfo("[" + (i + 1) + "] " + items.get(i));
        }
    }

    /**
     * Prints a standardised section header with bordered title.
     *
     * @param title the header title text
     */
    public void printHeader(String title) {
        printInfo("==================================================");
        printInfo("        " + title);
        printInfo("==================================================");
    }

    /**
     * Prints the standard footer separator.
     */
    public void printFooter() {
        printInfo("--------------------------------------------------");
    }

    /**
     * Clears the terminal screen to provide a clean transition between views.
     */
    public void clearScreen() {
        terminal.writer().print("\033[H\033[2J");
        terminal.writer().flush();
    }

    /**
     * Returns the currently active completer.
     *
     * <p>This is intended to be used by the delegating completer
     * wired into the {@link LineReader} so that completion suggestions
     * change dynamically as the prompt context changes.</p>
     *
     * @return the active completer, or {@code null} if none is set
     */
    public Completer getActiveCompleter() {
        return activeCompleter;
    }

    /**
     * Replaces the active completer used for subsequent prompts.
     *
     * @param completer the new completer to install
     */
    public void setActiveCompleter(Completer completer) {
        this.activeCompleter = completer;
    }

    /**
     * Helper para exibir opções de menu antigas.
     */
    public void printMenuOptions(String... options) {
        for (int i = 0; i < options.length; i++) {
            printInfo((i + 1) + " - " + options[i]);
        }
    }

    /**
     * Helper para ler inteiros do terminal com fallback de segurança.
     */
    public int readIntOption(String promptText) {
        String input = readString(promptText);
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
