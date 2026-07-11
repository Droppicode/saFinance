package com.safinance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.safinance.core.domain.Account;
import com.safinance.core.domain.CreditAccount;
import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.domain.WalletAccount;
import com.safinance.core.domain.AdminUser;
import com.safinance.core.domain.RegularUser;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.infra.persistence.JsonlRepository;
import com.safinance.infra.persistence.Repository;
import com.safinance.infra.persistence.PolymorphicTypeAdapterFactory;
import com.safinance.view.BaseMenu;
import com.safinance.view.WelcomeMenu;
import com.safinance.view.PromptService;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando ObjectFinance...\n");

        // 1. Inicializa dependências globais de utilidade (Gson com ensinamento para Interfaces)
        PolymorphicTypeAdapterFactory<Account> accountAdapterFactory = PolymorphicTypeAdapterFactory.of(Account.class)
            .registerSubtype(CreditAccount.class)
            .registerSubtype(WalletAccount.class)
            .registerSubtype(SavingsAccount.class);

        PolymorphicTypeAdapterFactory<User> userAdapterFactory = PolymorphicTypeAdapterFactory.of(User.class)
            .registerSubtype(RegularUser.class)
            .registerSubtype(AdminUser.class);

        Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(userAdapterFactory)
            .registerTypeAdapterFactory(accountAdapterFactory)
            .create();

        // 2. Cria a Infraestrutura (Onde instanciamos a implementação CONCRETA)
        // Repare que declaramos a variável apenas com a interface (Repository),
        // mas injetamos nela a implementação real (JsonlRepository).
        Repository<User, String> userRepository = new JsonlRepository<>("data/users.jsonl", User.class, gson);
        Repository<Account, String> accountRepository = new JsonlRepository<>("data/accounts.jsonl", Account.class, gson);

        // (Opcional) Salva um usuário fake só pra o teste rodar
        if (userRepository.findById("admin@safinance.com") == null) {
            userRepository.save(new RegularUser("admin@safinance.com", "Admin", "admin@safinance.com", "123456"));
        }

        // 3. INJEÇÃO DE DEPENDÊNCIA:
        // O núcleo de negócios (UseCase) é instanciado recebendo a infraestrutura pelo construtor.
        AuthUseCase authUseCase = new AuthUseCase(userRepository);
        UserUseCase userUseCase = new UserUseCase(userRepository);
        AccountUseCase accountUseCase = new AccountUseCase(accountRepository);

        // 4. Configurando Interface de Linha de Comando (JLine) e Inicializando
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            
            // Completer dinâmico para permitir suporte a autocompletar que muda a cada menu
            Completer dynamicCompleter = new Completer() {
                private PromptService ps;
                public void setPromptService(PromptService promptService) { this.ps = promptService; }
                @Override
                public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
                    if (ps != null && ps.getActiveCompleter() != null) {
                        ps.getActiveCompleter().complete(reader, line, candidates);
                    }
                }
            };
            
            LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(dynamicCompleter)
                .build();
                
            PromptService promptService = new PromptService(reader, terminal);
            
            // Gambiarra Java para inicializar referência cruzada
            try {
                dynamicCompleter.getClass().getMethod("setPromptService", PromptService.class).invoke(dynamicCompleter, promptService);
            } catch (Exception ignore) {}
            
            BaseMenu currentState = new WelcomeMenu(authUseCase, userUseCase, accountUseCase);
            
            // Loop principal da aplicação (State Machine)
            while (currentState != null) {
                promptService.clearScreen();
                currentState.renderHeader(promptService);
                
                promptService.setActiveCompleter(
                    new StringsCompleter(currentState.getOptions())
                );
                
                currentState = currentState.handleInput(promptService);
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erro fatal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
