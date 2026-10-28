package service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import exception.AccountNotFoundException;
import exception.BankException;
import model.Account;
import model.CurrentAccount;
import model.FixedDepositAccount;
import model.SavingsAccount;

public final class MiniBank {

    private MiniBank() {
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Path dataDirectory = Path.of("data");
        Path stateFile = dataDirectory.resolve("accounts.dat");
        Path logsDirectory = dataDirectory.resolve("logs");
        Path transactionLog = logsDirectory.resolve("transactions.log");
        Path reportFile = dataDirectory.resolve("end-of-day-report.txt");

        Account[] savedAccounts;
        if (Files.exists(stateFile)) {
            savedAccounts = StatePersister.load(stateFile);
            System.out.println("Loaded saved accounts from " + stateFile + ".");
        } else {
            savedAccounts = new Account[] {
                    new SavingsAccount("Ansh", 5000, 2000),
                    new CurrentAccount("Priyanka", 1500, 3000),
                    new FixedDepositAccount("Rahul", 1000)
            };
        }

        Repository<Account> repository = new Repository<>();
        repository.saveAll(savedAccounts);

        try (BankService bank = new BankService(repository, transactionLog);
                Scanner input = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                try {
                    int choice = readInteger(input, "Choose an option: ");
                    switch (choice) {
                        case 1 -> openAccount(input, bank);
                        case 2 -> deposit(input, bank);
                        case 3 -> withdraw(input, bank);
                        case 4 -> transfer(input, bank);
                        case 5 -> printStatement(input, bank);
                        case 6 -> printAccounts(bank);
                        case 7 -> printReport(logsDirectory, reportFile);
                        case 8 -> batchDeposits(input, bank);
                        case 9 -> running = false;
                        default -> System.out.println("Choose a menu option from 1 to 9.");
                    }
                } catch (BankException e) {
                    System.out.println("Bank operation failed: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println(
                            "The operation may have completed, but its transaction log could not be written: "
                                    + e.getMessage()
                    );
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid input: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Operation interrupted.");
                    running = false;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    System.out.println(
                            "A batch transaction failed: "
                                    + (cause == null ? e.getMessage() : cause.getMessage())
                    );
                }
            }

            bank.saveState(stateFile);
            printReport(logsDirectory, reportFile);
            System.out.println("Account state saved. Goodbye.");
        }
    }

    private static void printMenu() {
        System.out.println("\n---------- MiniBank ----------");
        System.out.println("1) Open account");
        System.out.println("2) Deposit");
        System.out.println("3) Withdraw");
        System.out.println("4) Transfer");
        System.out.println("5) Account statement");
        System.out.println("6) List accounts");
        System.out.println("7) Generate end-of-day report");
        System.out.println("8) Process batch deposits");
        System.out.println("9) Save and exit");
    }

    private static void openAccount(Scanner input, BankService bank) throws BankException {
        System.out.print("Owner name: ");
        String owner = input.nextLine().trim();
        System.out.print("Type (SAVINGS, CURRENT, FIXED): ");
        String type = input.nextLine().trim();
        long openingBalance = readLong(input, "Opening balance: ");
        Account account = bank.openAccount(owner, type, openingBalance);
        System.out.println("Account opened: " + account);
    }

    private static void deposit(Scanner input, BankService bank) throws BankException, IOException {
        String accountNumber = readAccountNumber(input, bank, "Deposit to");
        long amount = readLong(input, "Deposit amount: ");
        bank.deposit(accountNumber, amount);
        System.out.println("Deposit completed. New balance: " + bank.listAccounts().stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst().orElseThrow().getBalance());
    }

    private static void withdraw(Scanner input, BankService bank) throws BankException, IOException {
        String accountNumber = readAccountNumber(input, bank, "Withdraw from");
        long amount = readLong(input, "Withdrawal amount: ");
        bank.withdraw(accountNumber, amount);
        System.out.println("Withdrawal completed.");
    }

    private static void transfer(Scanner input, BankService bank) throws BankException, IOException {
        String from = readAccountNumber(input, bank, "Transfer from");
        String to = readAccountNumber(input, bank, "Transfer to");
        long amount = readLong(input, "Transfer amount: ");
        bank.transfer(from, to, amount);
        System.out.println("Transfer completed.");
    }

    private static void printStatement(Scanner input, BankService bank) throws AccountNotFoundException {
        String accountNumber = readAccountNumber(input, bank, "Statement for");
        System.out.println(bank.statement(accountNumber));
    }

    private static void printAccounts(BankService bank) {
        List<Account> accounts = bank.listAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts are available.");
            return;
        }
        System.out.println("Accounts (sorted by balance):");
        for (Account account : accounts) {
            System.out.println(account);
        }
    }

    private static void printReport(Path logsDirectory, Path reportFile) throws IOException {
        ReportGenerator.generate(logsDirectory, reportFile);
        System.out.println("End-of-day report written to " + reportFile + ":");
        System.out.println(Files.readString(reportFile));
    }

    private static void batchDeposits(Scanner input, BankService bank)
            throws BankException, InterruptedException, ExecutionException {
        int count = readInteger(input, "Number of deposits in this batch: ");
        if (count < 1) {
            throw new IllegalArgumentException("Batch size must be at least one.");
        }

        ArrayList<BankService.DepositRequest> deposits = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            System.out.println("Batch deposit " + (i + 1) + " of " + count);
            String accountNumber = readAccountNumber(input, bank, "Deposit to");
            long amount = readLong(input, "Deposit amount: ");
            deposits.add(new BankService.DepositRequest(accountNumber, amount));
        }
        bank.processBatchDeposits(deposits);
        System.out.println("Processed " + deposits.size() + " batch deposits.");
    }

    private static String readAccountNumber(Scanner input, BankService bank, String prompt)
            throws AccountNotFoundException {
        ArrayList<Account> accounts = bank.listAccounts();
        if (accounts.isEmpty()) {
            throw new AccountNotFoundException("No accounts are available.");
        }
        System.out.println(prompt + ":");
        for (Account account : accounts) {
            System.out.println(
                    account.getAccountNumber() + " | " + account.getOwnerName()
                            + " | balance=" + account.getBalance()
            );
        }
        System.out.print("Account number: ");
        return input.nextLine().trim();
    }

    private static int readInteger(Scanner input, String prompt) {
        System.out.print(prompt);
        String value = input.nextLine().trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a whole number.", e);
        }
    }

    private static long readLong(Scanner input, String prompt) {
        System.out.print(prompt);
        String value = input.nextLine().trim();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a whole number.", e);
        }
    }
}
