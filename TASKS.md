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
- [ ] Menu Usuário, Menu Contas, Menu Login
- [ ] Autenticação (login, logout, registro)
- [ ] Transação, crédito, débito
- [ ] Banco
- [ ] Menu admin
- Relatórios

- Investimentos