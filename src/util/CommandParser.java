package util;

import model.Command;
import model.TransactionType;

public class CommandParser {

    public static Command parse(String line) {

        String[] parts = line.split(" ");

        if (parts.length != 3) {

            throw new IllegalArgumentException(
                    "Invalid command expected 3 parts"
            );
        }

        TransactionType type =
                TransactionType.valueOf(parts[0]);

        String accountNumber = parts[1];

        long amount = Long.parseLong(parts[2]);

        return new Command(
                type,
                accountNumber,
                amount
        );
    }
}