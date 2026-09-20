package model;

public class FixedDepositAccount extends Account {

    public FixedDepositAccount(
            String owner_name,
            long opening_balance
    ) {

        super(owner_name, opening_balance);
    }

    @Override
    public double interestRate() {

        return 7.0;
    }

    @Override
    public boolean canWithdraw(long amount) {

        return false;
    }
}