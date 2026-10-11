package service;
import java.util.*;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.Account;
import model.BankInfo;
import model.Command;
import model.CurrentAccount;
import model.FixedDepositAccount;
import model.SavingsAccount;
import model.WithdrawRule;
import model.annotation.AnnotationValidator;

import util.CommandParser;
import util.StatementFormatter;
import util.Validator;

import static model.TransactionType.DEPOSIT;

public class MiniBank {

    enum MenuOption {
        OPEN_ACCOUNT,
        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        EXIT
    }

    public static void main(String[] args) {

        BankInfo bank = new BankInfo(
                "MiniBank",
                "Anand Branch"
        );

        Scanner s = new Scanner(System.in);
        boolean run = true;

        try (BankSession session = new BankSession()) {
            session.start();
        }

        Account[] accounts = new Account[3];

        accounts[0] = new SavingsAccount(
                "Ansh",
                5000,
                2000
        );

        accounts[1] = new CurrentAccount(
                "Priyanka",
                15000,
                3000
        );

        accounts[2] = new FixedDepositAccount(
                "Rahul",
                10000
        );

        Account sharedAccount = new SavingsAccount("Concurrent", 0, 0);
        Thread[] workers = new Thread[10];

        System.out.println("\nConcurrent Deposit Test");
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(
                    new AccountWorker(sharedAccount, 1000, 1),
                    "AccountWorker-" + (i + 1)
            );
            workers[i].start();
        }

        boolean workersCompleted = true;
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted while waiting for account workers.");
                workersCompleted = false;
                break;
            }
        }

        if (workersCompleted) {
            System.out.println("Final balance = " + sharedAccount.getBalance());
        }

        try {
            runTransactionEngineDemo();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interrupted while running transaction demonstrations.");
        }

        Account invalidAccount = new SavingsAccount("Invalid", -1000, 1000);
        String[] validationErrors = AnnotationValidator.validate(invalidAccount);

        System.out.println("\nValidation Errors");
        for (String error : validationErrors) {
            System.out.println(error);
        }

        System.out.println("\nAccount Interest Rates");

        for (Account account : accounts) {

            System.out.println(
                    account.getOwnerName()
                            + " : "
                            + account.interestRate()
                            + "%"
            );
        }

        if (accounts[0]
                instanceof SavingsAccount savings_account) {

            System.out.println(
                    "Savings Account found: "
                            + savings_account.getOwnerName()
            );
        }

        /*
         * Part B
         * InterestBearing interface
         */

        System.out.println("\nYearly Interest");

        for (Account account : accounts) {

            System.out.println(
                    account.getOwnerName()
                            + " : "
                            + account.yearlyInterest()
            );
        }

        /*
         * Part B
         * WithdrawRule using anonymous class
         */

        WithdrawRule anonymous_rule =
                new WithdrawRule() {

                    @Override
                    public boolean allow(
                            Account account,
                            long amount
                    ) {

                        return account.canWithdraw(amount);
                    }
                };

        System.out.println(
                "\nAnonymous Withdraw Rule: "
                        + anonymous_rule.allow(
                                accounts[0],
                                1000
                        )
        );

        /*
         * Part B
         * WithdrawRule using lambda
         */

        WithdrawRule lambda_rule =
                (account, amount) ->
                        account.canWithdraw(amount);

        System.out.println(
                "Lambda Withdraw Rule: "
                        + lambda_rule.allow(
                                accounts[1],
                                1000
                        )
        );

        /*
         * Validator Testing
         */

        System.out.println("\nValidator Testing");

        System.out.println(
                Validator.isValidMobile("9876543210")
        );

        System.out.println(
                Validator.isValidMobile("12345")
        );

        System.out.println(
                Validator.isValidEmail("abc@xyz.com")
        );

        System.out.println(
                Validator.isValidEmail("abcxyz.com")
        );

        System.out.println(
                Validator.isValidPan("ABCDE1234F")
        );

        System.out.println(
                Validator.isValidPan("ABC123")
        );

        System.out.println(
                Validator.isValidIfsc("SBIN0001234")
        );

        System.out.println(
                Validator.isValidIfsc("SBIN123")
        );

        System.out.println(
                Validator.isValidAmount("500")
        );

        System.out.println(
                Validator.isValidAmount("-500")
        );

        /*
         * Command Parser
         */

        Command command =
                CommandParser.parse(
                        "DEPOSIT AC0001 500"
                );

        /*
         * Static Import
         */

        if (command.type() == DEPOSIT) {

            System.out.println(
                    "\nStatic Import Used: DEPOSIT"
            );
        }

        System.out.println("\nParsed Command");

        System.out.println(
                "Type = "
                        + command.type()
        );

        System.out.println(
                "Account Number = "
                        + command.accountNumber()
        );

        System.out.println(
                "Amount = "
                        + command.amount()
        );

        /*
         * Account Statement
         */

        System.out.println("\nAccount Statement");

        System.out.println(
                StatementFormatter.buildStatement(
                        accounts[0]
                )
        );

        /*
         * Existing Menu
         */

        while (run) {

            System.out.println("\n--------------------");

            System.out.println(
                    bank.name()
                            + " - "
                            + bank.branch()
            );

            System.out.println("1) Open Account");
            System.out.println("2) Deposit");
            System.out.println("3) Withdraw");
            System.out.println("4) Transfer");
            System.out.println("5) Exit");

            System.out.print("Enter your choice:");

            int choice = s.nextInt();

            MenuOption option;

            switch (choice) {

                case 1:

                    option =
                            MenuOption.OPEN_ACCOUNT;

                    break;

                case 2:

                    option =
                            MenuOption.DEPOSIT;

                    break;

                case 3:

                    option =
                            MenuOption.WITHDRAW;

                    break;

                case 4:

                    option =
                            MenuOption.TRANSFER;

                    break;

                case 5:

                    option =
                            MenuOption.EXIT;

                    break;

                default:

                    option = null;
            }

            if (option == null) {

                System.out.println(
                        "Invalid Choice"
                );

                continue;
            }

            switch (option) {

                case OPEN_ACCOUNT -> {

                    System.out.println(
                            "\nAccounts Created Successfully"
                    );

                    System.out.println(
                            accounts[0]
                    );

                    System.out.println(
                            accounts[1]
                    );

                    System.out.println(
                            accounts[2]
                    );

                    System.out.println();

                    System.out.println(
                            "Account 1 equals Account 2 : "
                                    + accounts[0]
                                    .equals(accounts[1])
                    );

                    System.out.println(
                            "Account 1 equals Account 1 : "
                                    + accounts[0]
                                    .equals(accounts[0])
                    );

                    Object obj = accounts[0];

                    if (obj instanceof Account) {

                        System.out.println(
                                "obj is an Account"
                        );
                    }
                }

                case DEPOSIT -> {

                    System.out.println(
                            "Select Account"
                    );

                    System.out.println(
                            "1."
                                    + accounts[0]
                                    .getOwnerName()
                    );

                    System.out.println(
                            "2."
                                    + accounts[1]
                                    .getOwnerName()
                    );

                    System.out.println(
                            "3."
                                    + accounts[2]
                                    .getOwnerName()
                    );

                    int acc = s.nextInt();

                    System.out.print(
                            "Enter Amount:"
                    );

                    long amount =
                            s.nextLong();

                    try {
                        Account selected = getAccount(accounts, acc);
                        selected.deposit(amount);
                        System.out.println("Deposit successful.");
                        System.out.println("Balance =" + selected.getBalance());
                    } catch (AccountNotFoundException | InvalidAmountException e) {
                        System.out.println(e.getMessage());
                    } finally {
                        System.out.println("Deposit attempt complete.");
                    }
                }

                case WITHDRAW -> {

                    System.out.println(
                            "Select Account"
                    );

                    System.out.println(
                            "1. "
                                    + accounts[0]
                                    .getOwnerName()
                    );

                    System.out.println(
                            "2. "
                                    + accounts[1]
                                    .getOwnerName()
                    );

                    System.out.println(
                            "3. "
                                    + accounts[2]
                                    .getOwnerName()
                    );

                    int acc = s.nextInt();

                    System.out.print(
                            "Enter Amount:"
                    );

                    long amount =
                            s.nextLong();

                    Account selected = null;
                    try {
                        selected = getAccount(accounts, acc);
                        selected.withdraw(amount);
                        System.out.println("Withdrawal successful.");
                    } catch (AccountNotFoundException | InvalidAmountException e) {
                        System.out.println(e.getMessage());
                    } catch (InsufficientFundsException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Shortfall =" + e.getShortfall());
                    } finally {
                        System.out.println("Withdrawal attempt complete.");
                        if (selected != null) {
                            System.out.println("Balance =" + selected.getBalance());
                        }
                    }
                }

                case TRANSFER -> {

                    System.out.println("Select sender account");

                    System.out.println("1. " + accounts[0].getOwnerName());
                    System.out.println("2. " + accounts[1].getOwnerName());
                    System.out.println("3. " + accounts[2].getOwnerName());

                    int sender = s.nextInt();

                    System.out.println("Select receiver account");
                    System.out.println("1. " + accounts[0].getOwnerName());
                    System.out.println("2. " + accounts[1].getOwnerName());
                    System.out.println("3. " + accounts[2].getOwnerName());

                    int receiver = s.nextInt();

                    System.out.print("Enter Amount:");
                    long transferAmount = s.nextLong();

                    Account from = null;
                    Account to = null;
                    try {
                        from = getAccount(accounts, sender);
                        to = getAccount(accounts, receiver);
                        from.transfer(to, transferAmount);
                        System.out.println("Transfer Successful");
                        System.out.println("Sender Balance =" + from.getBalance());
                        System.out.println("Receiver Balance =" + to.getBalance());
                    } catch (InsufficientFundsException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Shortfall =" + e.getShortfall());
                    } catch (BankException e) {
                        System.out.println(e.getMessage());
                    } finally {
                        System.out.println("Transfer attempt complete.");
                    }
                }

                case EXIT -> {

                    System.out.println(
                            "Thank you for using MiniBank."
                    );

                    run = false;
                }
            }
        }

        s.close();
    }

    private static Account getAccount(Account[] accounts, int selection)
            throws AccountNotFoundException {
        if (selection < 1 || selection > accounts.length) {
            throw new AccountNotFoundException("Account selection was not found.");
        }

        return accounts[selection - 1];
    }

    private static void runTransactionEngineDemo() throws InterruptedException {
        System.out.println("\nManaged Transaction Processor");
        Account batchAccount = new SavingsAccount("Batch", 10000, 0);
        TransactionProcessor processor = new TransactionProcessor();

        for (int i = 0; i < 8; i++) {
            processor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println(threadName + " processing a batch transaction.");
                performDepositAndWithdrawal(batchAccount, 100, 50);
            });
        }

        System.out.println("\nProducer-Consumer Transactions");
        TransactionBuffer buffer = new TransactionBuffer(3);
        Account bufferAccount = new SavingsAccount("Buffer", 0, 0);
        Thread producer = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " started.");
            try {
                for (int i = 0; i < 8; i++) {
                    buffer.put(() -> performDepositAndWithdrawal(bufferAccount, 10, 5));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                buffer.finish();
                System.out.println(Thread.currentThread().getName() + " finished.");
            }
        }, "TransactionProducer");

        Thread consumer = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " started.");
            try {
                Runnable transaction;
                while ((transaction = buffer.take()) != null) {
                    transaction.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println(Thread.currentThread().getName() + " finished.");
            }
        }, "TransactionConsumer");

        producer.start();
        consumer.start();
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            buffer.finish();
            producer.interrupt();
            consumer.interrupt();
            processor.stop();
            throw e;
        }

        System.out.println("\nOpposing Account Transfers");
        Account accountA = new CurrentAccount("Transfer A", 5000, 10000);
        Account accountB = new CurrentAccount("Transfer B", 5000, 10000);

        processor.submit(() -> performRepeatedTransfers(accountA, accountB));
        processor.submit(() -> performRepeatedTransfers(accountB, accountA));

        processor.stop();
        System.out.println("Batch account balance = " + batchAccount.getBalance());
        System.out.println("Buffer account balance = " + bufferAccount.getBalance());
        System.out.println("Transfer A balance = " + accountA.getBalance());
        System.out.println("Transfer B balance = " + accountB.getBalance());
    }

    private static void performDepositAndWithdrawal(Account account, long deposit, long withdrawal) {
        try {
            account.deposit(deposit);
            account.withdraw(withdrawal);
        } catch (BankException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }

    private static void performRepeatedTransfers(Account from, Account to) {
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + " started opposing transfers.");

        for (int i = 0; i < 5000; i++) {
            try {
                from.transfer(to, 1);
            } catch (BankException e) {
                System.out.println("Transfer failed: " + e.getMessage());
                break;
            }
        }

        System.out.println(threadName + " finished opposing transfers.");
    }
}