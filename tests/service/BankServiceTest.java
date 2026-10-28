package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.Account;
import model.CurrentAccount;
import model.Customer;
import model.SavingsAccount;
import model.annotation.AnnotationValidator;
import util.Validator;

class BankServiceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void depositIncreasesBalance() throws Exception {
        SavingsAccount account = new SavingsAccount("Deposit test", 100, 0);
        Repository<Account> repository = new Repository<>();
        repository.save(account);

        try (BankService bank = new BankService(repository, tempDirectory.resolve("transactions.log"))) {
            bank.deposit(account.getAccountNumber(), 50);
            assertEquals(150, account.getBalance());
        }
    }

    @Test
    void withdrawalOverAvailableBalanceThrowsInsufficientFunds() throws Exception {
        CurrentAccount account = new CurrentAccount("Withdrawal test", 100, 0);
        Repository<Account> repository = new Repository<>();
        repository.save(account);

        try (BankService bank = new BankService(repository, tempDirectory.resolve("transactions.log"))) {
            assertThrows(
                    InsufficientFundsException.class,
                    () -> bank.withdraw(account.getAccountNumber(), 101)
            );
            assertEquals(100, account.getBalance());
        }
    }

    @Test
    void concurrentDepositsPreserveTheCorrectBalance() throws Exception {
        SavingsAccount account = new SavingsAccount("Concurrent test", 0, 0);
        int workerCount = 8;
        int depositsPerWorker = 1_000;
        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        try {
            List<Callable<Void>> deposits = new ArrayList<>();
            for (int worker = 0; worker < workerCount; worker++) {
                deposits.add(() -> {
                    for (int deposit = 0; deposit < depositsPerWorker; deposit++) {
                        account.deposit(1);
                    }
                    return null;
                });
            }

            List<Future<Void>> results = executor.invokeAll(deposits);
            for (Future<Void> result : results) {
                result.get();
            }
            assertEquals((long) workerCount * depositsPerWorker, account.getBalance());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void batchDepositsUseTheThreadSafeTransactionProcessor() throws Exception {
        SavingsAccount account = new SavingsAccount("Batch test", 0, 0);
        Repository<Account> repository = new Repository<>();
        repository.save(account);

        try (BankService bank = new BankService(repository, tempDirectory.resolve("batch.log"))) {
            bank.processBatchDeposits(List.of(
                    new BankService.DepositRequest(account.getAccountNumber(), 25),
                    new BankService.DepositRequest(account.getAccountNumber(), 75)
            ));
            assertEquals(100, account.getBalance());
        }
    }

    @Test
    void overflowingBatchIsRejectedBeforeAnyDepositIsApplied() throws Exception {
        SavingsAccount account = new SavingsAccount("Overflow test", Long.MAX_VALUE - 5, 0);
        Repository<Account> repository = new Repository<>();
        repository.save(account);

        try (BankService bank = new BankService(repository, tempDirectory.resolve("overflow.log"))) {
            assertThrows(
                    InvalidAmountException.class,
                    () -> bank.processBatchDeposits(List.of(
                            new BankService.DepositRequest(account.getAccountNumber(), 4),
                            new BankService.DepositRequest(account.getAccountNumber(), 2)
                    ))
            );
            assertEquals(Long.MAX_VALUE - 5, account.getBalance());
        }
    }

    @Test
    void validatorsRejectNullAndAccountLimitsDoNotOverflow() throws Exception {
        assertFalse(Validator.isValidMobile(null));
        assertFalse(Validator.isValidEmail(null));
        assertFalse(Validator.isValidPan(null));
        assertFalse(Validator.isValidIfsc(null));
        assertFalse(Validator.isValidAmount(null));

        CurrentAccount current = new CurrentAccount("Limit test", Long.MAX_VALUE, 10);
        assertTrue(current.canWithdraw(Long.MAX_VALUE));

        SavingsAccount savings = new SavingsAccount("Savings limit test", Long.MIN_VALUE, Long.MAX_VALUE);
        assertFalse(savings.canWithdraw(1));
    }

    @Test
    void maxLengthAnnotationIsReadFromCustomerFields() {
        Customer customer = new Customer(
                "X".repeat(101),
                "long@example.com",
                "9876543210",
                new Customer.Address("1 Bank Street", "Anand", "388001")
        );

        assertEquals(1, AnnotationValidator.validate(customer).length);
    }

    @Test
    void timestampedTransactionsAreLoggedAndLegacyLogsStillReport() throws IOException {
        Path log = tempDirectory.resolve("transactions.log");
        Path accountLog = tempDirectory.resolve("account-creations.log");
        Files.writeString(
                log,
                "DEPOSIT,AC0001,10" + System.lineSeparator()
                        + "WITHDRAW,AC0002,5" + System.lineSeparator(),
                StandardCharsets.UTF_8
        );
        TransactionLog.append(log, "TRANSFER,AC0001,AC0002,25");
        AccountCreationLog.append(accountLog, "AC0003");

        String[] entries = Files.readString(log, StandardCharsets.UTF_8).split("\\R");
        assertTrue(entries[2].matches("\\d{4}-\\d{2}-\\d{2}T[^,]+Z,TRANSFER,AC0001,AC0002,25"));
        assertTrue(Files.readString(accountLog, StandardCharsets.UTF_8)
                .matches("(?s)\\d{4}-\\d{2}-\\d{2}T[^,]+Z,ACCOUNT_OPENED,AC0003\\R?"));

        Path report = tempDirectory.resolve("report.txt");
        ReportGenerator.generate(tempDirectory, report);
        String reportText = Files.readString(report, StandardCharsets.UTF_8);
        assertTrue(reportText.contains("Total deposits: 35"));
        assertTrue(reportText.contains("Total withdrawals: 30"));
    }

    @Test
    void openingAnAccountWritesOnlyToAccountCreationLog() throws Exception {
        Repository<Account> repository = new Repository<>();
        Path transactionLog = tempDirectory.resolve("transactions.log");
        Path accountCreationLog = tempDirectory.resolve("account-creations.log");

        try (BankService bank = new BankService(repository, transactionLog, accountCreationLog)) {
            Account opened = bank.openAccount("New customer", "SAVINGS", 500);
            assertTrue(Files.readString(accountCreationLog, StandardCharsets.UTF_8)
                    .contains(",ACCOUNT_OPENED," + opened.getAccountNumber()));
            assertFalse(Files.exists(transactionLog));
        }

        Path report = tempDirectory.resolve("report.txt");
        ReportGenerator.generate(tempDirectory, report);
        String reportText = Files.readString(report, StandardCharsets.UTF_8);
        assertTrue(reportText.contains("Total deposits: 0"));
        assertTrue(reportText.contains("Total withdrawals: 0"));
    }
}
