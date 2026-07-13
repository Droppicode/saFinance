# SA Finance (ObjectFinance)

SA Finance is a simple Command Line Interface (CLI) application for financial management, built with Java. It allows users to manage different types of accounts, perform financial transactions, make investments, and generate reports.

## Key Features

*   **User Management:** Support for regular users and administrators.
*   **Account Management:** Manage wallet accounts, savings accounts, and credit accounts.
*   **Transactions:** Perform deposits, withdrawals, and transfers between accounts.
*   **Investments:** Simulate market investments (buy, sell, update prices) with different asset classes (crypto, stocks, real estate).
*   **Reporting:** View account statements, user reports, and system-wide financial summaries.
*   **Persistence:** Data is stored locally in JSON and JSONL files.

## Technical Requirements

*   Java 21 or higher
*   Maven 3.x

## How to Build and Run

1.  **Clone the repository** (or navigate to the project directory).
2.  **Compile the project:**
    ```bash
    mvn clean compile
    ```
3.  **Run the application:**
    ```bash
    mvn exec:java
    ```

## Running Tests

To execute the unit tests for the domain logic, run:
```bash
mvn test
```

## Architecture

The project follows Object-Oriented Programming (OOP) principles and implements several design patterns, such as:
*   **Template Method:** For standardized menu navigation.
*   **Command Pattern:** For modular menu actions.
*   **Visitor Pattern:** For user-specific reports.
*   **Factory Method:** For entity instantiation.

For more details on the architecture and design decisions, please refer to the `RELATORIO_TECNICO.md` and `ARCHITECTURE.md` files included in the project.
