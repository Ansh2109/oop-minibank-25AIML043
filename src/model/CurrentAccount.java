package model;

public class CurrentAccount extends Account {

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

        return getBalance() - amount >= -overdraft_limit;
    }
}