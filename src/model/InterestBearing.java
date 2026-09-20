package model;


public interface InterestBearing {

    default double yearlyInterest() {

        return interestRate() * 0.01 * getBalance();
    }

    double interestRate();

    long getBalance();
}