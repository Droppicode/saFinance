package com.safinance;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.safinance.core.domain.Account;
import com.safinance.core.domain.AdminUser;
import com.safinance.core.domain.Asset;
import com.safinance.core.domain.Bank;
import com.safinance.core.domain.BuyAssetTransaction;
import com.safinance.core.domain.CreditAccount;
import com.safinance.core.domain.ExpenseTransaction;
import com.safinance.core.domain.FixedIncome;
import com.safinance.core.domain.IncomeTransaction;
import com.safinance.core.domain.Market;
import com.safinance.core.domain.RealEstateFund;
import com.safinance.core.domain.RegularUser;
import com.safinance.core.domain.SavingsAccount;
import com.safinance.core.domain.SellAssetTransaction;
import com.safinance.core.domain.SimulatedAssetMarket;
import com.safinance.core.domain.Stock;
import com.safinance.core.domain.tax.TaxStrategy;
import com.safinance.core.domain.tax.StandardTax;
import com.safinance.core.domain.tax.ExemptTax;
import com.safinance.core.domain.AssetMarket;
import com.safinance.core.domain.Transaction;
import com.safinance.core.domain.TransactionFactory;
import com.safinance.core.domain.User;
import com.safinance.core.domain.WalletAccount;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.AuthUseCase;
import com.safinance.core.usecases.BankUseCase;
import com.safinance.core.usecases.InvestmentUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.core.usecases.UserUseCase;
import com.safinance.infra.persistence.JsonlRepository;
import com.safinance.infra.persistence.MarketStateRepository;
import com.safinance.infra.persistence.LocalDateTimeAdapter;
import com.safinance.infra.persistence.YearMonthAdapter;
import com.safinance.infra.persistence.PolymorphicTypeAdapterFactory;
import com.safinance.infra.persistence.Repository;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;
import com.safinance.view.menus.WelcomeMenu;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(new Locale("pt", "BR"));
        System.out.println("Iniciando ObjectFinance...\n");

        // 1. Inicializa dependências globais de utilidade (Gson com ensinamento para Interfaces)
        PolymorphicTypeAdapterFactory<Account> accountAdapterFactory = PolymorphicTypeAdapterFactory.of(Account.class)
            .registerSubtype(CreditAccount.class)
            .registerSubtype(WalletAccount.class)
            .registerSubtype(SavingsAccount.class);

        PolymorphicTypeAdapterFactory<User> userAdapterFactory = PolymorphicTypeAdapterFactory.of(User.class)
            .registerSubtype(RegularUser.class)
            .registerSubtype(AdminUser.class);

        PolymorphicTypeAdapterFactory<Asset> assetAdapterFactory = PolymorphicTypeAdapterFactory.of(Asset.class)
            .registerSubtype(Stock.class)
            .registerSubtype(RealEstateFund.class)
            .registerSubtype(FixedIncome.class);
        PolymorphicTypeAdapterFactory<Transaction> transactionAdapterFactory = PolymorphicTypeAdapterFactory.of(Transaction.class)
            .registerSubtype(IncomeTransaction.class)
            .registerSubtype(ExpenseTransaction.class)
            .registerSubtype(BuyAssetTransaction.class)
            .registerSubtype(SellAssetTransaction.class);

        PolymorphicTypeAdapterFactory<TaxStrategy> taxStrategyAdapterFactory = PolymorphicTypeAdapterFactory.of(TaxStrategy.class)
            .registerSubtype(StandardTax.class)
            .registerSubtype(ExemptTax.class);

        Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(YearMonth.class, new YearMonthAdapter())
            .registerTypeAdapterFactory(userAdapterFactory)
            .registerTypeAdapterFactory(accountAdapterFactory)
            .registerTypeAdapterFactory(assetAdapterFactory)
            .registerTypeAdapterFactory(transactionAdapterFactory)
            .registerTypeAdapterFactory(taxStrategyAdapterFactory)
            .create();

        // 2. Cria a Infraestrutura (Onde instanciamos a implementação CONCRETA)
        // Repare que declaramos a variável apenas com a interface (Repository),
        // mas injetamos nela a implementação real (JsonlRepository).
        Repository<User, String> userRepository = new JsonlRepository<>("data/users.jsonl", User.class, gson);
        Repository<Account, String> accountRepository = new JsonlRepository<>("data/accounts.jsonl", Account.class, gson);
        Repository<Transaction, String> transactionRepository = new JsonlRepository<>("data/transactions.jsonl", Transaction.class, gson);
        Repository<Bank, String> bankRepository = new JsonlRepository<>("data/bank.jsonl", Bank.class, gson);
        
        Bank bank = bankRepository.findById("BANK_SINGLETON");
        if (bank == null) {
            bank = new Bank(YearMonth.now(), 0.005);
            bankRepository.save(bank);
        }

        // Estado do mercado simulado: restaura preços e último movimento da execução
        // anterior e persiste a cada novo movimento. O tempo decorrido com o app
        // fechado é aplicado via catch-up quando a tela de investimentos abre.
        MarketStateRepository marketStateRepository = new MarketStateRepository("data/market.json", gson);
        MarketStateRepository.MarketState marketState = marketStateRepository.load();
        if (marketState != null) {
            AssetMarket.restoreState(marketState.getPrices(), marketState.getLastMove());
        }
        AssetMarket.setOnMove(() ->
            marketStateRepository.save(AssetMarket.snapshotPrices(), AssetMarket.lastMoveInstant()));

        // (Opcional) Salva um usuário fake só pra o teste rodar
        if (userRepository.findById("admin@safinance.com") == null) {
            userRepository.save(new AdminUser("admin@safinance.com", "Admin", "admin@safinance.com", "123456"));
        }

        // 3. INJEÇÃO DE DEPENDÊNCIA:
        // O núcleo de negócios (UseCase) é instanciado recebendo a infraestrutura pelo construtor.
        AuthUseCase authUseCase = new AuthUseCase(userRepository);
        UserUseCase userUseCase = new UserUseCase(userRepository);
        BankUseCase bankUseCase = new BankUseCase(bank, bankRepository);
        AccountUseCase accountUseCase = new AccountUseCase(accountRepository, bank);

        TransactionFactory transactionFactory = new TransactionFactory();
        TransactionUseCase transactionUseCase = new TransactionUseCase(accountRepository, transactionRepository, transactionFactory, bank);

        Market market = new SimulatedAssetMarket();
        InvestmentUseCase investmentUseCase = new InvestmentUseCase(accountRepository, transactionRepository, transactionFactory, market);

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
            MenuContext ctx = new MenuContext(authUseCase, userUseCase, bankUseCase, accountUseCase, investmentUseCase, transactionUseCase);
            BaseMenu currentState = new WelcomeMenu(ctx);
            
            // Loop principal da aplicação (State Machine)
            while (currentState != null) {
                promptService.clearScreen();
                currentState.renderHeader(promptService);
                
                promptService.setActiveCompleter(
                    new StringsCompleter(currentState.getOptions())
                );
                
                try {
                    currentState = currentState.handleInput(promptService);
                } catch (org.jline.reader.UserInterruptException | org.jline.reader.EndOfFileException e) {
                    System.out.println("\nOperação cancelada pelo usuário. Encerrando o sistema...");
                    break;
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erro fatal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}