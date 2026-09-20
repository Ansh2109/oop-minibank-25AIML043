package model;

public class SavingsAccount extends Account implements Premium {

    private long min_balance;

    public SavingsAccount(
            String owner_name,
            long opening_balance,
            long min_balance
    ) {

        super(owner_name, opening_balance);

        this.min_balance = min_balance;
    }

    @Override
    public double interestRate() {

        return 4.0;
    }

    @Override
    public boolean canWithdraw(long amount) {

        return getBalance() - amount >= min_balance;
    }
}