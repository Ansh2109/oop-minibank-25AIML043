# MiniBank

## Requirements
JDK 17+

## Compile
Run these commands from the project root in PowerShell:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

## Run
```powershell
java -cp out service.MiniBank
```

The program runs the concurrency demonstrations before showing the menu: a fixed
thread pool, a wait/notify producer-consumer buffer, and opposing transfers using
consistent account-lock ordering.

## Persistence and transaction reporting

On first startup, the sample accounts are saved to `data/accounts.dat` and
reloaded before the menu is shown. Later startups load the saved accounts;
balances are saved again on exit. Successful menu deposits, withdrawals, and
transfers are appended to `data/logs/transactions.log`. On exit, MiniBank walks
the logs folder and writes an end-of-day totals and file-details report to
`data/end-of-day-report.txt`.

Transaction log records use comma-separated `DEPOSIT,account,amount`,
`WITHDRAW,account,amount`, and `TRANSFER,source,destination,amount` entries.
Transfers contribute to both the deposit and withdrawal totals.