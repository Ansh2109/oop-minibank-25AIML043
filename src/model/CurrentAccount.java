package model;

import java.io.Serializable;

public class CurrentAccount extends Account implements Serializable {

    private static final long serialVersionUID = 1L;

    private long overdraft_limit;

    public CurrentAccount(
            String owner_name,
            long opening_balance,
            long overdraft_limit
    ) {

        super(owner_name, opening_balance);

        this.overdraft_limit = overdraft_limit;
    }

    @Override
    public double interestRate() {

        return 0;
    }

    @Override
    public boolean canWithdraw(long amount) {

        return amount > 0 && amount <= availableToWithdraw();
    }

    @Override
    protected long availableToWithdraw() {
        long balance = getBalance();
        if (overdraft_limit > 0 && balance > Long.MAX_VALUE - overdraft_limit) {
            return Long.MAX_VALUE;
        }
        if (overdraft_limit < 0 && balance < Long.MIN_VALUE - overdraft_limit) {
            return 0;
        }
        return Math.max(0, balance + overdraft_limit);
    }
}