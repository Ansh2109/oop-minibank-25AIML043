package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ReportGenerator {

    private ReportGenerator() {
    }

    public static void generate(Path logsDirectory, Path reportFile) throws IOException {
        long totalDeposits = 0;
        long totalWithdrawals = 0;
        List<String> fileDetails = new ArrayList<>();
        List<Path> logFiles = new ArrayList<>();

        Files.createDirectories(logsDirectory);
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(logsDirectory)) {
            for (Path entry : entries) {
                if (Files.isRegularFile(entry) && !entry.toAbsolutePath().equals(reportFile.toAbsolutePath())) {
                    logFiles.add(entry);
                }
            }
        }
        logFiles.sort(Comparator.comparing(path -> path.getFileName().toString()));

        for (Path logFile : logFiles) {
            BasicFileAttributes attributes = Files.readAttributes(logFile, BasicFileAttributes.class);
            FileTime lastModified = attributes.lastModifiedTime();
            fileDetails.add(
                    logFile.getFileName()
                            + " | size=" + attributes.size() + " bytes"
                            + " | last-modified=" + lastModified
            );

            try (BufferedReader reader = Files.newBufferedReader(logFile, StandardCharsets.UTF_8)) {
                String line;
                long lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    String[] fields = line.trim().split("[,\\s]+");
                    try {
                        if ("DEPOSIT".equals(fields[0]) && (fields.length == 2 || fields.length == 3)) {
                            totalDeposits = Math.addExact(
                                    totalDeposits,
                                    parseAmount(fields[fields.length - 1])
                            );
                        } else if ("WITHDRAW".equals(fields[0]) && (fields.length == 2 || fields.length == 3)) {
                            totalWithdrawals = Math.addExact(
                                    totalWithdrawals,
                                    parseAmount(fields[fields.length - 1])
                            );
                        } else if (fields.length == 4 && "TRANSFER".equals(fields[0])) {
                            long amount = parseAmount(fields[3]);
                            totalWithdrawals = Math.addExact(totalWithdrawals, amount);
                            totalDeposits = Math.addExact(totalDeposits, amount);
                        } else {
                            throw new IllegalArgumentException("Unrecognized transaction entry.");
                        }
                    } catch (IllegalArgumentException | ArithmeticException e) {
                        throw new IOException(
                                "Invalid transaction in " + logFile + " at line " + lineNumber + ": " + line,
                                e
                        );
                    }
                }
            }
        }

        StringBuilder report = new StringBuilder()
                .append("End-of-Day Transaction Summary").append(System.lineSeparator())
                .append("Total deposits: ").append(totalDeposits).append(System.lineSeparator())
                .append("Total withdrawals: ").append(totalWithdrawals).append(System.lineSeparator())
                .append("Log files:").append(System.lineSeparator());
        for (String details : fileDetails) {
            report.append(details).append(System.lineSeparator());
        }

        Path parent = reportFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(reportFile, report.toString(), StandardCharsets.UTF_8);
    }

    private static long parseAmount(String value) {
        long amount = Long.parseLong(value);
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amounts must be positive.");
        }
        return amount;
    }
}
