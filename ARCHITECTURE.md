# ObjectFinance - Documentação de Arquitetura

Este documento sintetiza as diretrizes, fluxos e regras de Orientação a Objetos estabelecidas para o projeto, alinhando-se aos critérios estipulados no "Projeto Final.pdf".

## 1. Estrutura de Pastas (Muros de Fronteira)
A arquitetura escolhida foi baseada no conceito de **Clean Architecture** (Arquitetura Limpa / Hexagonal), que difere sutilmente do modelo MVC tradicional (visto no Lab-4). 

Enquanto o Lab-4 organiza tudo horizontalmente (`model`, `view`, `controller`, `service`), a nossa estrutura prioriza o **isolamento absoluto da regra de negócio**, atendendo ao requisito do PDF de que *"persitência de dados e interfaces atuem como meros detalhes de infraestrutura, isoladas do núcleo por inversão de dependências"*.

* **`core.domain` (O Espaço do Problema)**: Contém as **entidades ricas** (sem setters/getters cegos) que atuam como máquinas de estado. Também guarda as interfaces abstratas e exceções customizadas de negócio.
* **`core.usecases`**: Serviços que orquestram a regra de negócio. Conhecem o domínio, mas desconhecem a infraestrutura.
* **`infra.persistence` (O Espaço da Solução)**: Camada responsável pela gravação (arquivos ou banco de dados). Ela *implementa* os contratos (interfaces) ditados pelo `domain`.
* **`view.console / view.gui`**: A interface de usuário, que interage unicamente por meio dos Casos de Uso.

## 2. Funcionalidades do Aplicativo (Casos de Uso)
Com base no domínio financeiro (ObjectFinance), o aplicativo deverá suportar os seguintes fluxos e interações:

1. **Gestão de Usuários**
   - Criação de novo usuário.
   - Login por usuário.
   - Perfil de Usuário Administrador (com permissões para gerenciar e visualizar todos os usuários).

2. **Gestão da Instituição Bancária (O Banco)**
   - Estrutura central administrada por usuários Admin (padrão Singleton ou Entidade Única).
   - Gerencia as **Taxas de Rendimento** mensais da poupança. Se um usuário consultar o rendimento e a taxa de um mês específico não existir (ex: pulou do mês 3 para o 7), o Banco gera as taxas dos meses faltantes aleatoriamente.
   - Fornece as **Taxas de Operação** (impostos/custos de transferência), relacionando-se diretamente com as Transações e o cálculo do *Strategy* (`TaxStrategy`).

3. **Gestão de Contas e Carteiras**
   - Criar novas contas bancárias vinculadas a um usuário (ex: `WalletAccount`, `CreditAccount` e `SavingsAccount`).
   - A `WalletAccount` atua tanto como Conta Corrente do dia a dia quanto como **Carteira de Investimentos**, guardando o `Portfolio` de ativos do usuário.
   - Consultar o saldo atual e o patrimônio investido.
   - Listar todas as contas cadastradas do usuário.

4. **Registro de Transações**
   - Registrar uma entrada financeira (Receita / `IncomeTransaction`).
   - Registrar uma saída financeira (Despesa / `ExpenseTransaction`), acionando validações de saldo (`Fail-Fast`).
   - Realizar transferências entre contas, com aplicação automática de taxas dependendo da estratégia escolhida (`TaxStrategy`).
   - **Atributos de uma Transação:** Cada transação deve conter id único, valor (montante), data da operação, descrição/categoria, e a referência à conta afetada.

5. **Área de Investimentos e Impostos (Capital Gains)**
   - Integração de compra e venda de Ativos (`Asset`), como Ações (`Stock`), Fundos Imobiliários (`RealEstateFund`) e Renda Fixa (`FixedIncome`).
   - O `Portfolio` da `WalletAccount` rastreia a posição do usuário, guardando a quantidade e o **Preço Médio** de compra de cada ativo.
   - **Tributação de Lucros (Baseada na Realidade):** As vendas de investimentos utilizam o padrão *Strategy* (`CapitalGainsTaxStrategy`) para calcular o imposto sobre o lucro:
     - **Ações:** Regras de isenção para pequenos volumes e alíquota fixa sobre o lucro quando o volume de vendas supera o teto estipulado.
     - **Fundos Imobiliários (FIIs):** Alíquota incidindo diretamente sobre qualquer lucro, sem isenção de volume.
   - Ações de investimento debitam/creditam o saldo e geram transações específicas (`AssetPurchaseTransaction`, `AssetSaleTransaction`).

6. **Geração de Relatórios (Extratos)**
   - Gerar extrato detalhado de uma conta (lista de todas as transações de um período).
   - Gerar balanço financeiro global, consolidando receitas e despesas utilizando o `Template Method` para formatação.

7. **Persistência de Dados**
   - Salvar o estado completo do sistema (usuários, contas e transações) em um arquivo (JSON/Texto).
   - Carregar/Restaurar os dados previamente salvos na inicialização do sistema.
