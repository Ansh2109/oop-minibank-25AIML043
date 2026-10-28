package service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.Account;
import model.BankInfo;
import model.CurrentAccount;
import model.Customer;
import model.FixedDepositAccount;
import model.SavingsAccount;
import model.WithdrawRule;
import model.annotation.AnnotationValidator;
import util.CommandParser;
import util.Validator;

import static model.TransactionType.DEPOSIT;

public final class MiniBank {

    private enum MenuOption {
        OPEN_ACCOUNT,
        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        STATEMENT,
        LIST_ACCOUNTS,
        REPORT,
        BATCH,
        LAB_DEMOS,
        EXIT,
        INVALID
    }

    private MiniBank() {
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Path dataDirectory = Path.of("data");
        Path stateFile = dataDirectory.resolve("accounts.dat");
        Path logsDirectory = dataDirectory.resolve("logs");
        Path transactionLog = logsDirectory.resolve("transactions.log");
        Path accountCreationLog = logsDirectory.resolve("account-creations.log");
        Path reportFile = dataDirectory.resolve("end-of-day-report.txt");
        Files.createDirectories(logsDirectory);
        if (Files.notExists(transactionLog)) {
            Files.createFile(transactionLog);
        }
        if (Files.notExists(accountCreationLog)) {
            Files.createFile(accountCreationLog);
        }

        Account[] savedAccounts;
        if (Files.exists(stateFile)) {
            savedAccounts = StatePersister.load(stateFile);
            System.out.println("Loaded saved accounts from " + stateFile + ".");
        } else {
            savedAccounts = new Account[] {
                    new SavingsAccount("Ansh", 10000, 2000),
                    new CurrentAccount("Tirth", 2000, 3000),
                    new FixedDepositAccount("Jugal", 3000)
            };
        }

        if (restoreSampleAccountNames(savedAccounts)) {
            StatePersister.save(savedAccounts, stateFile);
            System.out.println("Updated saved sample account names.");
        }

        try (BankSession session = new BankSession()) {
            session.start();
        }

        Repository<Account> repository = new Repository<>();
        repository.saveAll(savedAccounts);
        BankInfo bankInfo = new BankInfo("MiniBank", "Anand Branch");

        try (BankService bank = new BankService(repository, transactionLog, accountCreationLog);
                Scanner input = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu(bankInfo);
                try {
                    int choice = readInteger(input, "Choose an option: ");
                    MenuOption option = switch (choice) {
                        case 1 -> MenuOption.OPEN_ACCOUNT;
                        case 2 -> MenuOption.DEPOSIT;
                        case 3 -> MenuOption.WITHDRAW;
                        case 4 -> MenuOption.TRANSFER;
                        case 5 -> MenuOption.STATEMENT;
                        case 6 -> MenuOption.LIST_ACCOUNTS;
                        case 7 -> MenuOption.REPORT;
                        case 8 -> MenuOption.BATCH;
                        case 9 -> MenuOption.EXIT;
                        case 10 -> MenuOption.LAB_DEMOS;
                        default -> MenuOption.INVALID;
                    };
                    switch (option) {
                        case OPEN_ACCOUNT -> openAccount(input, bank);
                        case DEPOSIT -> deposit(input, bank);
                        case WITHDRAW -> withdraw(input, bank);
                        case TRANSFER -> transfer(input, bank);
                        case STATEMENT -> printStatement(input, bank);
                        case LIST_ACCOUNTS -> printAccounts(bank);
                        case REPORT -> printReport(logsDirectory, reportFile);
                        case BATCH -> batchDeposits(input, bank);
                        case LAB_DEMOS -> showLabDemonstrations();
                        case EXIT -> running = false;
                        case INVALID -> System.out.println("Choose a menu option from 1 to 10.");
                    }
                } catch (BankException e) {
                    System.out.println("Bank operation failed: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println(
                            "The operation may have completed, but its log could not be written: "
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
                } finally {
                    System.out.println("Menu operation complete.");
                }
            }

            bank.saveState(stateFile);
            printReport(logsDirectory, reportFile);
            System.out.println("Account state saved. Goodbye.");
        }
    }

    private static void printMenu(BankInfo bankInfo) {
        System.out.println("\n---------- " + bankInfo.name() + " | " + bankInfo.branch() + " ----------");
        System.out.println("1) Open account");
        System.out.println("2) Deposit");
        System.out.println("3) Withdraw");
        System.out.println("4) Transfer");
        System.out.println("5) Account statement");
        System.out.println("6) List accounts");
        System.out.println("7) Generate end-of-day report");
        System.out.println("8) Process batch deposits");
        System.out.println("9) Save and exit");
        System.out.println("10) Show lab demonstrations");
    }

    private static void showLabDemonstrations() throws InterruptedException {
        System.out.println("\n--- Model and Validation Demonstrations ---");
        Account sample = new SavingsAccount("Demo", 1000, 100);
        Account otherSample = new SavingsAccount("Other demo", 1000, 100);
        System.out.println("toString: " + sample);
        System.out.println("equals/hashCode: " + sample.equals(otherSample) + " / " + sample.hashCode());
        System.out.println("instanceof Account: " + (sample instanceof Account));

        Customer customer = new Customer(
                "Demo Customer",
                "demo@example.com",
                "9876543210",
                new Customer.Address("1 Bank Street", "Anand", "388001")
        );
        System.out.println(
                "Customer clone address city: " + customer.clone().getAddress().getCity()
        );
        Customer longNameCustomer = new Customer(
                "X".repeat(101),
                "long@example.com",
                "9876543210",
                new Customer.Address("1 Bank Street", "Anand", "388001")
        );
        System.out.println(
                "Max-length annotation errors: "
                        + Arrays.toString(AnnotationValidator.validate(longNameCustomer))
        );
        System.out.println("Valid mobile: " + Validator.isValidMobile("9876543210"));
        System.out.println("Invalid mobile: " + Validator.isValidMobile("12345"));
        System.out.println("Null mobile rejected: " + !Validator.isValidMobile(null));
        System.out.println("Valid email: " + Validator.isValidEmail("demo@example.com"));
        System.out.println("Invalid email: " + Validator.isValidEmail("demo.example.com"));
        System.out.println("Valid PAN: " + Validator.isValidPan("ABCDE1234F"));
        System.out.println("Invalid PAN: " + Validator.isValidPan("ABC123"));
        System.out.println("Valid IFSC: " + Validator.isValidIfsc("SBIN0001234"));
        System.out.println("Invalid IFSC: " + Validator.isValidIfsc("SBIN123"));
        model.Command parsed = CommandParser.parse("DEPOSIT AC0001 500");
        System.out.println("Parsed command: " + parsed);
        System.out.println("Static import DEPOSIT matched: " + (parsed.type() == DEPOSIT));

        Account[] accountTypes = {
                new SavingsAccount("Savings demo", 1000, 100),
                new CurrentAccount("Current demo", 1000, 500),
                new FixedDepositAccount("Fixed demo", 1000)
        };
        for (Account account : accountTypes) {
            System.out.println(
                    account.getOwnerName() + " interest rate: " + account.interestRate() + "%"
            );
            System.out.println("Yearly interest: " + account.yearlyInterest());
        }
        System.out.println(
                "Savings pattern match: "
                        + (accountTypes[0] instanceof SavingsAccount savings && savings.canWithdraw(100))
        );

        WithdrawRule anonymousRule = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                return account.canWithdraw(amount);
            }
        };
        WithdrawRule lambdaRule = (account, amount) -> account.canWithdraw(amount);
        System.out.println("Anonymous WithdrawRule: " + anonymousRule.allow(sample, 100));
        System.out.println("Lambda WithdrawRule: " + lambdaRule.allow(sample, 100));
        System.out.println(
                "Negative-balance annotation errors: "
                        + Arrays.toString(
                                AnnotationValidator.validate(new SavingsAccount("Invalid demo", -1, 0))
                        )
        );
        try {
            accountTypes[2].withdraw(1);
        } catch (InsufficientFundsException e) {
            System.out.println("Checked withdrawal exception: " + e.getMessage());
        } catch (InvalidAmountException e) {
            System.out.println("Invalid withdrawal amount: " + e.getMessage());
        }

        System.out.println("\n--- Collections and Repository Demonstrations ---");
        AccountStore store = new AccountStore();
        for (Account account : accountTypes) {
            store.add(account);
        }
        for (Account account : store.sortedByBalance()) {
            System.out.println("Sorted account: " + account);
        }

        Repository<Account> repository = new Repository<>();
        repository.saveAll(accountTypes);
        System.out.println("Repository lookup: " + repository.findById(
                accountTypes[0].getAccountNumber()
        ));
        System.out.println("Repository account count: " + repository.findAll().size());
        System.out.println(
                "Average sample: " + Repository.average(new Integer[] {10, 20, 30})
        );

        System.out.println("\n--- Thread and Buffer Demonstrations ---");
        Account workerAccount = new SavingsAccount("Worker demo", 0, 0);
        Thread[] workers = new Thread[10];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(new AccountWorker(workerAccount, 1000, 1), "AccountWorker-" + (i + 1));
            workers[i].start();
        }
        joinWorkers(workers);
        System.out.println("Synchronized worker balance: " + workerAccount.getBalance());

        Account bufferedAccount = new SavingsAccount("Buffer demo", 0, 0);
        TransactionBuffer buffer = new TransactionBuffer(2);
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    buffer.put(() -> {
                        try {
                            bufferedAccount.deposit(1);
                        } catch (InvalidAmountException e) {
                            throw new IllegalStateException("Buffer demo transaction failed.", e);
                        }
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                buffer.finish();
            }
        }, "DemoProducer");
        Thread consumer = new Thread(() -> {
            try {
                Runnable task;
                while ((task = buffer.take()) != null) {
                    task.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "DemoConsumer");
        producer.start();
        consumer.start();
        joinWorkers(new Thread[] {producer, consumer});
        System.out.println("Producer-consumer balance: " + bufferedAccount.getBalance());

        TransactionProcessor processor = new TransactionProcessor();
        Account taskAccount = new CurrentAccount("Pool task demo", 1000, 0);
        processor.addAccount(taskAccount);
        for (int i = 0; i < 8; i++) {
            processor.submit(() -> performDemoDepositAndWithdrawal(
                    processor.getAccount(taskAccount.getAccountNumber())
            ));
        }

        Account transferA = new CurrentAccount("Transfer demo A", 1000, 1000);
        Account transferB = new CurrentAccount("Transfer demo B", 1000, 1000);
        processor.addAccount(transferA);
        processor.addAccount(transferB);
        processor.submit(() -> performDemoTransfers(
                processor.getAccount(transferA.getAccountNumber()),
                processor.getAccount(transferB.getAccountNumber())
        ));
        processor.submit(() -> performDemoTransfers(
                processor.getAccount(transferB.getAccountNumber()),
                processor.getAccount(transferA.getAccountNumber())
        ));
        processor.stop();
        System.out.println("Managed task balance: " + taskAccount.getBalance());
        System.out.println("Opposing transfer balances: " + transferA.getBalance() + " / " + transferB.getBalance());
    }

    private static void joinWorkers(Thread[] workers) throws InterruptedException {
        try {
            for (Thread worker : workers) {
                worker.join();
            }
        } catch (InterruptedException e) {
            for (Thread worker : workers) {
                worker.interrupt();
            }
            throw e;
        }
    }

    private static void performDemoTransfers(Account from, Account to) {
        for (int i = 0; i < 1000; i++) {
            try {
                from.transfer(to, 1);
            } catch (BankException e) {
                throw new IllegalStateException("Opposing transfer demo failed.", e);
            }
        }
    }

    private static void performDemoDepositAndWithdrawal(Account account) {
        try {
            account.deposit(10);
            account.withdraw(5);
        } catch (BankException e) {
            throw new IllegalStateException("Managed transaction demo failed.", e);
        }
    }

    private static boolean restoreSampleAccountNames(Account[] accounts) {
        boolean changed = false;
        for (Account account : accounts) {
            if ("AC0002".equals(account.getAccountNumber())
                    && "Priyanka".equals(account.getOwnerName())) {
                account.renameOwner("Tirth");
                changed = true;
            } else if ("AC0003".equals(account.getAccountNumber())
                    && "Rahul".equals(account.getOwnerName())) {
                account.renameOwner("Jugal");
                changed = true;
            }
        }
        return changed;
    }

    private static void openAccount(Scanner input, BankService bank) throws BankException, IOException {
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
