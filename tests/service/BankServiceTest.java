package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import model.SavingsAccount;

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
}
