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
        return Math.max(0, getBalance() + overdraft_limit);
    }
}