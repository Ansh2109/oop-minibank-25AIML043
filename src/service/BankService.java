package service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InvalidAmountException;
import model.Account;
import model.CurrentAccount;
import model.FixedDepositAccount;
import model.SavingsAccount;
import util.StatementFormatter;
import util.Validator;

public final class BankService implements AutoCloseable {

    private final Repository<Account> accounts;
    private final TransactionProcessor transactionProcessor;
    private final Path transactionLog;

    public BankService(Repository<Account> accounts, Path transactionLog) {
        this.accounts = Objects.requireNonNull(accounts, "Account repository cannot be null.");
        this.transactionLog = Objects.requireNonNull(transactionLog, "Transaction log path cannot be null.");
        transactionProcessor = new TransactionProcessor();
        ArrayList<Account> storedAccounts = accounts.findAll();
        Account.synchronizeAccountCounter(storedAccounts);
        for (Account account : storedAccounts) {
            transactionProcessor.addAccount(account);
        }
    }

    public synchronized Account openAccount(String ownerName, String accountType, long openingBalance)
            throws InvalidAmountException {
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Account owner name cannot be blank.");
        }
        if (accountType == null || accountType.isBlank()) {
            throw new IllegalArgumentException("Account type cannot be blank.");
        }
        validateNonNegativeAmount(openingBalance);

        Account account = switch (accountType.toUpperCase(Locale.ROOT)) {
            case "SAVINGS" -> new SavingsAccount(ownerName.trim(), openingBalance, 0);
            case "CURRENT" -> new CurrentAccount(ownerName.trim(), openingBalance, 0);
            case "FIXED" -> new FixedDepositAccount(ownerName.trim(), openingBalance);
            default -> throw new IllegalArgumentException(
                    "Account type must be SAVINGS, CURRENT, or FIXED."
            );
        };
        accounts.save(account);
        transactionProcessor.addAccount(account);
        return account;
    }

    public synchronized void deposit(String accountNumber, long amount)
            throws BankException, IOException {
        validatePositiveAmount(amount);
        Account account = requireAccount(accountNumber);
        account.deposit(amount);
        recordTransaction("DEPOSIT," + accountNumber + "," + amount);
    }

    public synchronized void withdraw(String accountNumber, long amount)
            throws BankException, IOException {
        validatePositiveAmount(amount);
        Account account = requireAccount(accountNumber);
        account.withdraw(amount);
        recordTransaction("WITHDRAW," + accountNumber + "," + amount);
    }

    public synchronized void transfer(String fromAccountNumber, String toAccountNumber, long amount)
            throws BankException, IOException {
        validatePositiveAmount(amount);
        Account source = requireAccount(fromAccountNumber);
        Account destination = requireAccount(toAccountNumber);
        source.transfer(destination, amount);
        recordTransaction(
                "TRANSFER," + fromAccountNumber + "," + toAccountNumber + "," + amount
        );
    }

    public synchronized String statement(String accountNumber) throws AccountNotFoundException {
        return StatementFormatter.buildStatement(requireAccount(accountNumber));
    }

    public synchronized ArrayList<Account> listAccounts() {
        ArrayList<Account> result = accounts.findAll();
        result.sort(
                Comparator.comparingLong(Account::getBalance)
                        .thenComparing(Account::compareTo)
        );
        return result;
    }

    public synchronized void processBatchDeposits(List<DepositRequest> deposits)
            throws InvalidAmountException, AccountNotFoundException, InterruptedException, ExecutionException {
        Objects.requireNonNull(deposits, "Deposit batch cannot be null.");
        ArrayList<Callable<Void>> transactions = new ArrayList<>();
        Map<String, Long> totalsByAccount = new HashMap<>();
        for (DepositRequest deposit : deposits) {
            Objects.requireNonNull(deposit, "Deposit batch cannot contain null entries.");
            validatePositiveAmount(deposit.amount());
            Account account = requireAccount(deposit.accountNumber());
            long total;
            try {
                total = Math.addExact(
                        totalsByAccount.getOrDefault(deposit.accountNumber(), 0L),
                        deposit.amount()
                );
            } catch (ArithmeticException e) {
                throw new InvalidAmountException(
                        "Combined batch deposits exceed the account balance limit."
                );
            }
            if (account.getBalance() > Long.MAX_VALUE - total) {
                throw new InvalidAmountException(
                        "Combined batch deposits exceed the account balance limit."
                );
            }
            totalsByAccount.put(deposit.accountNumber(), total);
            transactions.add(() -> {
                Account transactionAccount = transactionProcessor.getAccount(deposit.accountNumber());
                if (transactionAccount == null) {
                    throw new AccountNotFoundException(
                            "Account was not found: " + deposit.accountNumber()
                    );
                }
                transactionAccount.deposit(deposit.amount());
                try {
                    recordTransaction(
                            "DEPOSIT," + deposit.accountNumber() + "," + deposit.amount()
                    );
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "Deposit completed, but transaction logging failed for "
                                    + deposit.accountNumber(),
                            e
                    );
                }
                return null;
            });
        }
        transactionProcessor.processBatch(transactions);
    }

    public synchronized void saveState(Path file) throws IOException {
        StatePersister.save(accounts.findAll().toArray(new Account[0]), file);
    }

    private Account requireAccount(String accountNumber) throws AccountNotFoundException {
        Account account = accounts.findById(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account was not found: " + accountNumber);
        }
        return account;
    }

    private void validatePositiveAmount(long amount) throws InvalidAmountException {
        if (!Validator.isValidAmount(Long.toString(amount)) || amount == 0) {
            throw new InvalidAmountException("Amount must be a positive whole number.");
        }
    }

    private void validateNonNegativeAmount(long amount) throws InvalidAmountException {
        if (amount < 0 || !Validator.isValidAmount(Long.toString(amount))) {
            throw new InvalidAmountException("Opening balance must be zero or a positive whole number.");
        }
    }

    private void recordTransaction(String line) throws IOException {
        TransactionLog.append(transactionLog, line);
    }

    @Override
    public void close() throws InterruptedException {
        transactionProcessor.stop();
    }

    public record DepositRequest(String accountNumber, long amount) {
    }
}
