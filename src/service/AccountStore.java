package service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

import model.Account;

public class AccountStore {

    private final HashMap<String, Account> accounts = new HashMap<>();
    private final ArrayList<String> transactionHistory = new ArrayList<>();

    public void add(Account account) {
        Objects.requireNonNull(account, "Account cannot be null.");
        Account existing = accounts.putIfAbsent(account.getAccountNumber(), account);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Account already exists: " + account.getAccountNumber()
            );
        }
    }

    public Account get(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public ArrayList<Account> getAll() {
        ArrayList<Account> allAccounts = new ArrayList<>(accounts.values());
        allAccounts.sort(Comparator.naturalOrder());
        return allAccounts;
    }

    public void addTransaction(String transaction) {
        transactionHistory.add(Objects.requireNonNull(transaction, "Transaction cannot be null."));
    }

    public ArrayList<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    public ArrayList<Account> sortedByBalance() {
        ArrayList<Account> sortedAccounts = getAll();
        sortedAccounts.sort(
                Comparator.comparingLong(Account::getBalance)
                        .thenComparing(Account::compareTo)
        );
        return sortedAccounts;
    }
}
