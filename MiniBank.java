import java.util.*;
import java.util.regex.Pattern;

public class MiniBank {

    enum MenuOption {
        OPEN_ACCOUNT,
        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        EXIT
    }

    record BankInfo(String name, String branch) {
    }

    public static void main(String[] args) {

        BankInfo bank = new BankInfo("MiniBank", "Anand Branch");

        Scanner s = new Scanner(System.in);
        boolean run = true;
        Account[] accounts = new Account[3];

        accounts[0] = new Account("Ansh", 5000);
        accounts[1] = new Account("Rahul", 10000);
        accounts[2] = new Account("Priya");

        System.out.println("\nValidator Testing");

System.out.println(Validator.isValidMobile("9876543210"));
System.out.println(Validator.isValidMobile("12345"));

System.out.println(Validator.isValidEmail("abc@xyz.com"));
System.out.println(Validator.isValidEmail("abcxyz.com"));

System.out.println(Validator.isValidPan("ABCDE1234F"));
System.out.println(Validator.isValidPan("ABC123"));

System.out.println(Validator.isValidIfsc("SBIN0001234"));
System.out.println(Validator.isValidIfsc("SBIN123"));

System.out.println(Validator.isValidAmount("500"));
System.out.println(Validator.isValidAmount("-500"));

Command command = CommandParser.parse("DEPOSIT AC0001 500");

System.out.println("\nParsed Command");
System.out.println("Type = " + command.type());
System.out.println("Account Number = " + command.accountNumber());
System.out.println("Amount = " + command.amount());

System.out.println("\nAccount Statement");
System.out.println(StatementFormatter.buildStatement(accounts[0]));

        while (run) {

            System.out.println("\n--------------------");
            System.out.println(bank.name() + " - " + bank.branch());
            System.out.println("1) Open Account");
            System.out.println("2) Deposit");
            System.out.println("3) Withdraw");
            System.out.println("4) Transfer");
            System.out.println("5) Exit");

            System.out.print("Enter your choice:");

            int choice = s.nextInt();

            MenuOption option;

            switch (choice) {
                case 1:
                    option = MenuOption.OPEN_ACCOUNT;
                    break;
                case 2:
                    option = MenuOption.DEPOSIT;
                    break;
                case 3:
                    option = MenuOption.WITHDRAW;
                    break;
                case 4:
                    option = MenuOption.TRANSFER;
                    break;
                case 5:
                    option = MenuOption.EXIT;
                    break;
                default:
                    option = null;
            }

            if (option == null) {
                System.out.println("Invalid Choice");
                continue;
            }

            switch (option) {

                case OPEN_ACCOUNT -> {
                    System.out.println("\nAccounts Created Successfully");

                    System.out.println(accounts[0]);

                    System.out.println(accounts[1]);

                    System.out.println(accounts[2]);
                    System.out.println();

                    System.out.println("Account 1 equals Account 2 : " + accounts[0].equals(accounts[1]));

                    System.out.println("Account 1 equals Account 1 : " + accounts[0].equals(accounts[0]));
                    Object obj = accounts[0];

                    if (obj instanceof Account) {
                        System.out.println("obj is an Account");
                    }
                }

                case DEPOSIT -> {

                    System.out.println("Select Account");
                    System.out.println("1." + accounts[0].getOwnerName());
                    System.out.println("2." + accounts[1].getOwnerName());
                    System.out.println("3." + accounts[2].getOwnerName());

                    int acc = s.nextInt();

                    System.out.print("Enter Amount:");
                    long amount = s.nextLong();

                    switch (acc) {

                        case 1:
                            accounts[0].deposit(amount);
                            System.out.println("Balance =" + accounts[0].getBalance());
                            break;

                        case 2:
                            accounts[1].deposit(amount);
                            System.out.println("Balance =" + accounts[1].getBalance());
                            break;

                        case 3:
                            accounts[2].deposit(amount);
                            System.out.println("Balance =" + accounts[2].getBalance());
                            break;

                        default:
                            System.out.println("Invalid Account");
                    }
                }

                case WITHDRAW -> {

                    System.out.println("Select Account");
                    System.out.println("1. " + accounts[0].getOwnerName());
                    System.out.println("2. " + accounts[1].getOwnerName());
                    System.out.println("3. " + accounts[2].getOwnerName());

                    int acc = s.nextInt();

                    System.out.print("Enter Amount:");
                    long amount = s.nextLong();

                    switch (acc) {

                        case 1:

                            if (accounts[0].withdraw(amount))
                                System.out.println("Withdrawal Successful");
                            else
                                System.out.println("Insufficient Balance");

                            System.out.println("Balance =" + accounts[0].getBalance());
                            break;

                        case 2:

                            if (accounts[1].withdraw(amount))
                                System.out.println("Withdrawal Successful");
                            else
                                System.out.println("Insufficient Balance");

                            System.out.println("Balance =" + accounts[1].getBalance());
                            break;

                        case 3:

                            if (accounts[2].withdraw(amount))
                                System.out.println("Withdrawal Successful");
                            else
                                System.out.println("Insufficient Balance");

                            System.out.println("Balance =" + accounts[2].getBalance());
                            break;

                        default:
                            System.out.println("Invalid Account");
                    }
                }

                case TRANSFER -> {

                    System.out.println("Transfer feature will be implemented later");
                }

                case EXIT -> {

                    System.out.println("Thank you for using MiniBank.");
                    run = false;
                }
            }
        }

        s.close();
    }
}

class Customer implements Cloneable {

    private String name;
    private String email;
    private String mobile;
    private final String customerId;
    private Address address;

    private static long customerCounter = 100;

    private static String generateCustomerId() {
        customerCounter++;
        return "CUST" + customerCounter;
    }

    public Customer(String name, String email, String mobile, Address address) { 
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
        this.customerId = generateCustomerId();
    }

    public Address getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getCustomerId() {
        return customerId;
    }

    @Override
    public Customer clone() {

        try {
            return (Customer) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public static class Address {

        private String line;
        private String city;
        private String pincode;

        public Address(String line, String city, String pincode) {
            this.line = line;
            this.city = city;
            this.pincode = pincode;
        }

        public String getLine() {
            return line;
        }

        public String getCity() {
            return city;
        }

        public String getPincode() {
            return pincode;
        }
    }
}

class Account {

    private final String accountNumber;
    private String ownerName;
    private long balance;
    private boolean active;

    private static int accountCounter = 0;

    private static String generateAccountNumber() {

        accountCounter++;
        return String.format("AC%04d", accountCounter);
    }

    public Account(String ownerName, long openingBalance) {

        this.ownerName = ownerName;
        this.balance = openingBalance;
        this.active = true;
        this.accountNumber = generateAccountNumber();
    }

    public Account(String ownerName) {

        this(ownerName, 0);
    }

    public void deposit(long amount) {

        if (amount > 0) {
            balance += amount;
        }
    }

    public boolean withdraw(long amount) {

        if (amount <= balance) {
            balance -= amount;
            return true;
        }

        return false;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return accountNumber + " | " + ownerName + " | Balance = " + balance;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (!(o instanceof Account))
            return false;

        Account a = (Account) o;

        return accountNumber.equals(a.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

}
class Validator {

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("[6-9][0-9]{9}");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PAN_PATTERN =
            Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]");

    private static final Pattern IFSC_PATTERN =
            Pattern.compile("[A-Z]{4}0[A-Z0-9]{6}");

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("[1-9][0-9]*");

    public static boolean isValidMobile(String mobile) {
        return MOBILE_PATTERN.matcher(mobile).matches();
    }

    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPan(String pan) {
        return PAN_PATTERN.matcher(pan).matches();
    }

    public static boolean isValidIfsc(String ifsc) {
        return IFSC_PATTERN.matcher(ifsc).matches();
    }

    public static boolean isValidAmount(String amount) {
        return AMOUNT_PATTERN.matcher(amount).matches();
    }
}

enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER
}

record Command(TransactionType type, String accountNumber, long amount) {
}

class CommandParser {

    public static Command parse(String line) {

        String[] parts = line.split(" ");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid command. Expected 3 parts.");
        }

        TransactionType type = TransactionType.valueOf(parts[0]);
        String accountNumber = parts[1];
        long amount = Long.parseLong(parts[2]);

        return new Command(type, accountNumber, amount);
    }
}

class StatementFormatter {

    public static String buildStatement(Account account) {

        StringBuilder statement = new StringBuilder();

        statement.append("------ Account Statement ------\n");
        statement.append("Account Number: ");
        statement.append(account.getAccountNumber());
        statement.append("\n");

        statement.append("Owner Name: ");
        statement.append(account.getOwnerName());
        statement.append("\n");

        statement.append("Balance: ");
        statement.append(account.getBalance());
        statement.append("\n");

        statement.append("Active: ");
        statement.append(account.isActive());
        statement.append("\n");

        statement.append("-------------------------------");

        return statement.toString();
    }
}

