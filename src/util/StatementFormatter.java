package util;

import model.Account;

public class StatementFormatter {

    public static String buildStatement(Account account) {

        StringBuilder statement = new StringBuilder();

        statement.append("------ Account Statement ------\n");

        statement.append("Account Number:");
        statement.append(account.getAccountNumber());
        statement.append("\n");

        statement.append("Owner Name:");
        statement.append(account.getOwnerName());
        statement.append("\n");

        statement.append("Balance:");
        statement.append(account.getBalance());
        statement.append("\n");

        statement.append("Interest Rate:");
        statement.append(account.interestRate());
        statement.append("%\n");

        statement.append("Active:");
        statement.append(account.isActive());
        statement.append("\n");

        statement.append("-------------------------------");

        return statement.toString();
    }
}