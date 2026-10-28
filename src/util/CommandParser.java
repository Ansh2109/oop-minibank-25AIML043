package util;

import model.Command;
import model.TransactionType;

import java.util.Locale;

public class CommandParser {

    public static Command parse(String line) {

        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Command cannot be empty"
            );
        }

        String[] parts = line.trim().split("\\s+");

        if (parts.length != 3) {

            throw new IllegalArgumentException(
                    "Invalid command expected 3 parts"
            );
        }

        TransactionType type =
                TransactionType.valueOf(parts[0].toUpperCase(Locale.ROOT));

        String accountNumber = parts[1];

        long amount;

        try {
            amount = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Amount must be a valid number"
            );
        }

        return new Command(
                type,
                accountNumber,
                amount
        );
    }
}