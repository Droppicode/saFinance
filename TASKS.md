# Tarefas

Roteiro de tarefas em ordem. Quando pegar uma tarefa, defina as subtarefas de acordo com o resto do projeto.

- [x] ArquivoJsonl (persistência de dados)
    - [x] Entity
    - [x] JsonlRepository
    - [x] Repository
    - [x] Injeção de Dependência (substituiu Repositories)
- [x] User, Admin
    - [x] Criação do Enum Role (REGULAR, ADMIN)
    - [x] Criação da interface User (extends Entity)
    - [x] Implementação estritamente imutável de RegularUser e AdminUser
    - [x] Inserção de validação Fail-Fast (lançando IllegalArgumentException) nos construtores
    - [x] Equals e HashCode amarrados na identidade (ID)
- [x] Contas (wallet, credit, savings)
    - [x] Criação da interface Account
    - [x] Implementação estritamente imutável da WalletAccount usando o Padrão Wither
    - [x] Implementação da CreditAccount com checagem de limite de crédito
    - [x] Implementação da SavingsAccount preparada para receber juros mensais
    - [x] Criação de Stubs vitais (Asset, AssetPosition, Bank, Transaction) para compilar
    - [x] Persistência concreta funcionando (serialização com type)
- [ ] Menu Usuário, Menu Contas, Menu Login
    - [x] Fluxo de autenticação
    - [x] Opções básicas para o usuário regular
    - [x] Listagem de contas formatada em tabela.
    - [x] Criação de conta (já chama CreateAccountMenu, mas precisa consolidar persistência).
    - [ ] Operações de transação (depósito, retirada, transferência).
    = [ ] Aplicar rendimento em poupança.
- [ ] Transação, crédito, débito
- [ ] Banco
    - [ ] Criar entidade Bank como instância única injetada 
    - [ ] Rendimento da poupança
        - [ ] Map<YearMonth, Double> yieldRates
        - [ ] getYieldRate(YearMonth) — retorna a taxa do mês
        - [ ] Geração aleatória das taxas dos meses faltantes (ex: pulou do mês 3 pro 7, gera 4,5,6,7)
        - [ ] setYieldRate(YearMonth, double) — configuração manual pelo admin
    - [ ] Taxas de operação (transferência) via Strategy
        - [ ] Interface TaxStrategy + implementações StandardTax e ExemptTax
        - [ ] operationCost(double amount, String tipo) no Bank 
    - [ ] BankUseCase — orquestra configuração (updateYieldRate, updateOperationTax) com injeção do repositório
    - [ ] Instanciar o Bank no Main e injetar em quem precisa (AccountUseCase; depois TransactionUseCase p/ transferência)
    - [ ] (Opcional) Persistir o estado do Bank (taxas/rendimentos) — bank.jsonl
- [ ] Menu admin
- Relatórios

- Investimentos
