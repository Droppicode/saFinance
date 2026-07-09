# Diagrama de Classes UML 

Este diagrama representa a arquitetura completa do `ObjectFinance`.

```mermaid
classDiagram
    %% ----------------------------------------------------
    %% CORE DOMAIN - Interfaces Base (Espaço do Problema)
    %% ----------------------------------------------------
    class Identifiable {
        <<interface>>
        +String getId()
    }
    class User {
        <<interface>>
        +String getName()
        +String getEmail()
        +boolean checkPassword(String password)
        +boolean isAdmin()
    }
    class Account {
        <<interface>>
        +String getOwnerId()
        +double getBalance()
        +process(Transaction t)
    }
    class Transaction {
        <<interface>>
        +double getAmount()
        +LocalDateTime getDate()
        +String getDescription()
        +String getAccountId()
    }
    class Asset {
        <<interface>>
        +String getTicker()
        +String getName()
    }
    class Bank {
        <<Singleton>>
        -Map~YearMonth, Double~ yieldRates
        -Map~String, Double~ operationTaxes
        -Map~String, Double~ assetPrices
        +double getYieldRate(YearMonth month)
        +double getOperationTax(String operation)
        +double getAssetPrice(String ticker)
        +void setYieldRate(YearMonth month, double rate)
        -generateRandomYieldRate(YearMonth month) double
    }
    Identifiable <|-- User
    Identifiable <|-- Account
    Identifiable <|-- Transaction
    Identifiable <|-- Asset
    Identifiable <|-- Bank

    %% ----------------------------------------------------
    %% DOMAIN - Implementações de Entidades
    %% ----------------------------------------------------
    class RegularUser {
        -String id
        -String name
        -String email
        -String passwordHash
    }
    class AdminUser {
        -String id
        -String name
        -String email
        -String passwordHash
    }
    User <|.. RegularUser
    User <|.. AdminUser

    class AssetPosition {
        -Asset asset
        -double quantity
        -double averagePrice
        -LocalDateTime firstPurchaseDate
        +updatePosition(double qty, double price)
    }

    class WalletAccount {
        -String id
        -String ownerId
        -double balance
        -Map~String, AssetPosition~ portfolio
        +process(Transaction t)
        +addAsset(Asset a, double quantity, double price)
        +removeAsset(Asset a, double quantity)
    }
    class CreditAccount {
        -String id
        -String ownerId
        -double balance
        -double creditLimit
        +process(Transaction t)
    }
    class SavingsAccount {
        -String id
        -String ownerId
        -double balance
        +process(Transaction t)
        +applyMonthlyYield(YearMonth month, Bank bank)
    }
    Account <|.. WalletAccount
    Account <|.. CreditAccount
    Account <|.. SavingsAccount

    class Stock {
        -String id
        -String ticker
        -String companyName
    }
    class RealEstateFund {
        -String id
        -String ticker
        -String sector
    }
    class FixedIncome {
        -String id
        -String ticker
        -double fixedYield
    }
    Asset <|.. Stock
    Asset <|.. RealEstateFund
    Asset <|.. FixedIncome

    class IncomeTransaction {
        -String id
        -double amount
        -String accountId
        -String description
        -LocalDateTime date
    }
    class ExpenseTransaction {
        -String id
        -double amount
        -String accountId
        -String description
        -LocalDateTime date
    }
    class AssetPurchaseTransaction {
        -String id
        -double amount
        -String assetId
        -double quantity
        -String accountId
        -LocalDateTime date
    }
    class AssetSaleTransaction {
        -String id
        -double amount
        -String assetId
        -double quantity
        -String accountId
        -LocalDateTime date
    }
    Transaction <|.. IncomeTransaction
    Transaction <|.. ExpenseTransaction
    Transaction <|.. AssetPurchaseTransaction
    Transaction <|.. AssetSaleTransaction

    %% Relacionamentos Estruturais do Domínio
    User "1" *-- "*" Account : owns (possui)
    Account "1" *-- "*" Transaction : history (contém)
    WalletAccount "1" *-- "*" AssetPosition : portfolio
    AssetPosition --> Asset : refers
    SavingsAccount ..> Bank : requests yield

    %% ----------------------------------------------------
    %% PADRÕES DE PROJETO (Design Patterns)
    %% ----------------------------------------------------
    class TaxStrategy {
        <<interface>>
        +double calculateTax(double amount, Bank bank)
    }
    class StandardTax
    class ExemptTax
    TaxStrategy <|.. StandardTax
    TaxStrategy <|.. ExemptTax
    TaxStrategy ..> Bank : reads tax rates

    class CapitalGainsTaxStrategy {
        <<interface>>
        +double calculateTax(Asset asset, double totalSale, double profit, int holdingDays)
    }
    class StockTaxStrategy
    class FIITaxStrategy
    CapitalGainsTaxStrategy <|.. StockTaxStrategy
    CapitalGainsTaxStrategy <|.. FIITaxStrategy

    class TransactionFactory {
        +Transaction createIncome(double amount, String accId, String desc)
        +Transaction createExpense(double amount, String accId, String desc)
        +Transaction createAssetPurchase(double amount, String assetId, double qty, String accId)
        +Transaction createAssetSale(double amount, String assetId, double qty, String accId)
    }
    TransactionFactory ..> Transaction : creates (instancia)

    class FinancialStatementTemplate {
        <<abstract>>
        +generateReport(Account acc, List~Transaction~ txs)
        #formatHeader(Account acc)
        #formatBody(List~Transaction~ txs)
        #formatFooter()
    }
    class UserDetailedStatement
    class GlobalBalanceStatement
    FinancialStatementTemplate <|-- UserDetailedStatement
    FinancialStatementTemplate <|-- GlobalBalanceStatement
    FinancialStatementTemplate ..> Account : reads
    FinancialStatementTemplate ..> Transaction : reads

    %% ----------------------------------------------------
    %% INFRAESTRUTURA - Persistência (O Espaço da Solução)
    %% ----------------------------------------------------
    class DataRepository~T~ {
        <<interface>>
        +save(T entity)
        +T findById(String id)
        +List~T~ findAll()
    }
    class JsonRepository~T~ {
        -String filePath
        +save(T entity)
        +T findById(String id)
        +List~T~ findAll()
    }
    DataRepository <|.. JsonRepository
    DataRepository ..> Identifiable : manages (gerencia)

    %% ----------------------------------------------------
    %% CASOS DE USO (Services / Application Layer)
    %% ----------------------------------------------------
    class AuthUseCase {
        +User login(String email, String password)
    }
    class UserUseCase {
        +User createUser(String name, String email, String password, boolean isAdmin)
    }
    class AccountUseCase {
        +Account createAccount(User user, String type)
        +List~Account~ listUserAccounts(User user)
        +applyYield(SavingsAccount acc, YearMonth month)
    }
    class TransactionUseCase {
        +void transfer(Account from, Account to, double amount, TaxStrategy tax)
        +void deposit(Account to, double amount)
    }
    class BankUseCase {
        +updateYieldRate(YearMonth month, double rate)
        +updateOperationTax(String operation, double rate)
        +updateAssetPrice(String ticker, double newPrice)
    }
    class InvestmentUseCase {
        +void buyAsset(WalletAccount acc, String ticker, double quantity)
        +void sellAsset(WalletAccount acc, String ticker, double quantity)
    }
    
    %% Casos de uso dependem do Repositório Abstrato (Inversão de Dependência)
    AuthUseCase ..> DataRepository : uses
    UserUseCase ..> DataRepository : uses
    AccountUseCase ..> DataRepository : uses
    TransactionUseCase ..> DataRepository : uses
    BankUseCase ..> DataRepository : uses
    InvestmentUseCase ..> DataRepository : uses (Assets)
    
    %% Os Casos de uso conversam com o Bank e Orquestram Padrões
    BankUseCase ..> Bank : configures
    AccountUseCase ..> Bank : fetches yield
    TransactionUseCase ..> TransactionFactory : triggers
    TransactionUseCase ..> TaxStrategy : applies
    TransactionUseCase ..> Account : updates balance
    
    InvestmentUseCase ..> Bank : fetches prices
    InvestmentUseCase ..> TransactionFactory : triggers purchase/sale
    InvestmentUseCase ..> CapitalGainsTaxStrategy : calculates imposto de renda
    InvestmentUseCase ..> WalletAccount : updates portfolio (AssetPosition)

    %% ----------------------------------------------------
    %% INTERFACE DE USUÁRIO (View Layer / Console)
    %% ----------------------------------------------------
    class ConsoleView {
        +start()
    }
    class LoginMenu {
        +showMenu()
    }
    class UserMenu {
        +showMenu(User session)
    }
    class AdminMenu {
        +showMenu(AdminUser session)
    }
    class InvestmentMenu {
        +showMenu(WalletAccount sessionAccount)
    }
    
    %% A MainView delega para os sub-menus
    ConsoleView --> LoginMenu : delegates
    ConsoleView --> UserMenu : delegates
    ConsoleView --> AdminMenu : delegates
    UserMenu --> InvestmentMenu : delegates

    %% Os Menus comunicam-se EXCLUSIVAMENTE com os Casos de Uso
    LoginMenu ..> AuthUseCase : calls
    UserMenu ..> AccountUseCase : calls
    UserMenu ..> TransactionUseCase : calls
    UserMenu ..> FinancialStatementTemplate : calls (gera extrato)
    AdminMenu ..> UserUseCase : calls
    AdminMenu ..> BankUseCase : manages
    InvestmentMenu ..> InvestmentUseCase : calls
```

> **Dica:** Você pode copiar e colar esse código no [Mermaid Live Editor](https://mermaid.live).
