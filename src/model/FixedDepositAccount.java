package model;

import java.io.Serializable;

public class FixedDepositAccount extends Account implements Serializable {

    private static final long serialVersionUID = 1L;

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

    @Override
    protected long availableToWithdraw() {
        return 0;
    }
}