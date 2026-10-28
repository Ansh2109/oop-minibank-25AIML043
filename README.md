# MiniBank

A Java 17 console banking application demonstrating object-oriented design,
collections, serialization, NIO reports, repositories, exception handling, and
safe concurrent transaction processing.

## Requirements

- JDK 17 or newer
- Apache Maven 3.9 or newer

## Build, test, and package

From the project root:

```powershell
mvn clean test package
```

The JUnit tests cover successful deposits, withdrawals exceeding the available
balance, and concurrent deposits. The build creates the runnable JAR at
`target/minibank-1.0.0.jar`.

## Run

```powershell
java -jar target/minibank-1.0.0.jar
```

Choose a menu option to open an account, deposit, withdraw, transfer, view an
account statement, list accounts, generate the report, or process a batch of
deposits. Select **9** to save and exit.

## Persistence and reports

Data is stored relative to the directory from which the JAR is run:

- `data/accounts.dat` — serialized accounts, loaded at startup and saved at exit
- `data/logs/transactions.log` — append-only successful transaction entries
- `data/end-of-day-report.txt` — deposit/withdrawal totals and log-file details

The report is available from menu option **7** and is also regenerated on exit.
Transfers contribute to both the deposit and withdrawal totals.

## Project highlights

- `BankService` centralizes account operations and validates amounts with
  `Validator`.
- `Repository<T>` stores entities by their runtime `@Id` field.
- `TransactionProcessor` uses a `ConcurrentHashMap` and a fixed thread pool for
  concurrent batch deposits.
- `ReportGenerator` walks the log directory using NIO and writes the daily
  summary.
