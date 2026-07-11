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
import com.safinance.core.domain.RegularUser;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.infra.persistence.JsonlRepository;
import com.safinance.infra.persistence.Repository;
import com.safinance.infra.persistence.PolymorphicTypeAdapterFactory;
import com.safinance.view.BaseMenu;
import com.safinance.view.LoginMenu;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando ObjectFinance...\n");

        // 1. Inicializa dependências globais de utilidade (Gson com ensinamento para Interfaces)
        PolymorphicTypeAdapterFactory<Account> accountAdapterFactory = PolymorphicTypeAdapterFactory.of(Account.class)
            .registerSubtype(CreditAccount.class)
            .registerSubtype(WalletAccount.class)
            .registerSubtype(SavingsAccount.class);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(User.class, (JsonDeserializer<User>) (json, typeOfT, context) -> {
                // Dizemos ao Gson: "Quando pedirem um User, instancie um RegularUser para evitar erro de Interface"
                // No futuro, podemos ler um campo "role" aqui no JSON para decidir se é AdminUser ou RegularUser.
                return context.deserialize(json, RegularUser.class);
            })
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

        // 4. Executando o Caso de Uso
        try {
            BaseMenu currentState = new LoginMenu(authUseCase, userUseCase, accountUseCase);
            
            // Loop principal da aplicação (State Machine)
            while (currentState != null) {
                currentState = currentState.render();
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erro fatal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
