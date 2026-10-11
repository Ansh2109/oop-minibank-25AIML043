package model;

import java.util.Objects;

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
    public void deposit(long amount) {

        if (amount > 0) {
            balance += amount;
        }
    }

    @Override
    public boolean withdraw(long amount) {

        if (canWithdraw(amount)) {

            balance -= amount;

            return true;
        }

        return false;
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

    public boolean transferTo(Account target, long amount) {

        if (target == null || !this.isActive() || !target.isActive()) {
            return false;
        }

        if (!this.canWithdraw(amount) || amount <= 0) {
            return false;
        }

        if (target == this) {
            return false;
        }

        this.balance -= amount;
        target.balance += amount;

        return true;
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