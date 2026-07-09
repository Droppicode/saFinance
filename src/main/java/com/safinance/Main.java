package com.safinance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.safinance.core.domain.RegularUser;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.infra.persistence.JsonlRepository;
import com.safinance.infra.persistence.Repository;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando ObjectFinance...\n");

        // 1. Inicializa dependências globais de utilidade (Gson com ensinamento para Interfaces)
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(User.class, (JsonDeserializer<User>) (json, typeOfT, context) -> {
                // Dizemos ao Gson: "Quando pedirem um User, instancie um RegularUser para evitar erro de Interface"
                // No futuro, podemos ler um campo "role" aqui no JSON para decidir se é AdminUser ou RegularUser.
                return context.deserialize(json, RegularUser.class);
            })
            .create();

        // 2. Cria a Infraestrutura (Onde instanciamos a implementação CONCRETA)
        // Repare que declaramos a variável apenas com a interface (Repository),
        // mas injetamos nela a implementação real (JsonlRepository).
        Repository<User, String> userRepository = new JsonlRepository<>("data/users.jsonl", User.class, gson);

        // (Opcional) Salva um usuário fake só pra o teste rodar
        if (userRepository.findById("admin@safinance.com") == null) {
            userRepository.save(new RegularUser("admin@safinance.com", "Admin", "admin@safinance.com", "123456"));
        }

        // 3. INJEÇÃO DE DEPENDÊNCIA:
        // O núcleo de negócios (UseCase) é instanciado recebendo a infraestrutura pelo construtor.
        AuthUseCase authUseCase = new AuthUseCase(userRepository);

        // 4. Executando o Caso de Uso
        try {
            User loggedIn = authUseCase.login("admin@safinance.com", "123456");
            System.out.println("✅ Sucesso! Usuário logado: " + loggedIn.getName());
            
            // ---> INICIANDO O MENU DE TESTE <---
            new com.safinance.view.TestMenu().start();
            
        } catch (Exception e) {
            System.out.println("❌ Falha no login: " + e.getMessage());
        }
    }
}
