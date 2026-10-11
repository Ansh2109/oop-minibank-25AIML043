package model;

import java.util.Objects;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.annotation.Id;
import model.annotation.Positive;

public abstract class Account implements Transactable, InterestBearing {

    @Id
    private final String accountNumber;
    private String ownerName;
    @Positive
    private long balance;
    private boolean active;

    private static int accountCounter = 0;

    private static String generateAccountNumber() {

        accountCounter++;

        return String.format(
                "AC%04d",
                accountCounter
        );
    }

    public Account(String owner_name, long opening_balance) {

        this.ownerName = owner_name;
        this.balance = opening_balance;
        this.active = true;
        this.accountNumber = generateAccountNumber();
    }

    public Account(String owner_name) {

        this(owner_name, 0);
    }

    @Override
    public void deposit(long amount) throws InvalidAmountException {

        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero.");
        }

        if (balance > Long.MAX_VALUE - amount) {
            throw new InvalidAmountException("Deposit amount exceeds the account balance limit.");
        }

        balance += amount;
    }

    @Override
    public boolean withdraw(long amount)
            throws InsufficientFundsException, InvalidAmountException {

        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }

        if (!canWithdraw(amount)) {
            long available = Math.max(0, availableToWithdraw());
            long shortfall = amount > available ? amount - available : 0;
            throw new InsufficientFundsException(
                    "Insufficient funds. Shortfall: " + shortfall,
                    shortfall
            );
        }

        balance -= amount;

        return true;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public abstract double interestRate();

    public abstract boolean canWithdraw(long amount);

    protected long availableToWithdraw() {
        return Math.max(0, balance);
    }

    public void transfer(Account target, long amount) throws BankException {
        if (target == null) {
            throw new AccountNotFoundException("Destination account was not found.");
        }

        if (target == this) {
            throw new InvalidAmountException("Cannot transfer to the same account.");
        }

        if (!isActive() || !target.isActive()) {
            throw new AccountNotFoundException("Source or destination account is inactive.");
        }

        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero.");
        }

        if (target.balance > Long.MAX_VALUE - amount) {
            throw new InvalidAmountException("Transfer amount exceeds the destination balance limit.");
        }

        boolean debited = false;
        boolean completed = false;

        try {
            withdraw(amount);
            debited = true;
            target.deposit(amount);
            completed = true;
        } catch (BankException e) {
            throw e;
        } finally {
            if (debited && !completed) {
                balance += amount;
            }
        }
    }

    @Override
    public String toString() {

        return accountNumber + " | " + ownerName + " | Balance = " + balance;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (!(o instanceof Account))
            return false;

        Account a = (Account) o;

        return accountNumber.equals(a.accountNumber);
    }

    @Override
    public int hashCode() {

        return Objects.hash(accountNumber);
    }
}