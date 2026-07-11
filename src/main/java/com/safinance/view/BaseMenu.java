    package com.safinance.view;

    import java.util.Scanner;

    /**
     * Classe abstrata que representa um menu base para a aplicação.
     */
    public abstract class BaseMenu {
        protected Scanner scanner = new Scanner(System.in);

        /**
         * Exibe o cabeçalho do menu com o título fornecido.
         * @param title O título a ser exibido no cabeçalho do menu.
         */
        protected void printHeader(String title) {
            clearScreen();
            System.out.println("\n==================================");
            System.out.println("   " + title);
            System.out.println("==================================");
        }

        /**
         * Exibe as opções do menu fornecidas como parâmetros.
         * @param options As opções a serem exibidas no menu.
         */
        protected void printOptions(String... options) {
            for (int i = 0; i < options.length; i++) {
                System.out.println((i+1) + " - " + options[i]);
            }
            System.out.println("0 - Voltar/Sair");
            System.out.print("> Escolha uma opção: ");
        }

        /**
         * Lê a opção escolhida pelo usuário no menu.
         * @return O número da opção escolhida pelo usuário.
         */
        protected int readOption() {
            int option = scanner.nextInt();
            scanner.nextLine(); // consome o \n pendente
            return option;
        }

        /**
         * Limpa a tela do console.
         */
        protected void clearScreen() {
            try {
                if (System.getProperty("os.name").contains("Windows")) {
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                } else {
                    new ProcessBuilder("clear").inheritIO().start().waitFor();
                }
            } catch (Exception e) {
                // fallback
                System.out.println("\n".repeat(50));
            }
        }

        /**
         * Método abstrato que deve ser implementado pelas subclasses para exibir o menu específico.
         */
        public abstract void showMenu();
    }

