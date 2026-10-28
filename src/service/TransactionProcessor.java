package service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import model.Account;

public class TransactionProcessor {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final ConcurrentHashMap<String, Account> sharedAccounts = new ConcurrentHashMap<>();

    public void submit(Runnable task) {
        executor.execute(task);
    }

    public void addAccount(Account account) {
        Objects.requireNonNull(account, "Account cannot be null.");
        Account existing = sharedAccounts.putIfAbsent(account.getAccountNumber(), account);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Account already exists: " + account.getAccountNumber()
            );
        }
    }

    public Account getAccount(String accountNumber) {
        return sharedAccounts.get(accountNumber);
    }

    public ArrayList<Account> getAccounts() {
        return new ArrayList<>(sharedAccounts.values());
    }

    public void stop() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Transaction processor did not terminate.");
            }
        }
    }
}
