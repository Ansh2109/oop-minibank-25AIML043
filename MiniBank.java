import java.util.*;

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