package service;
import java.util.*;

import model.Account;
import model.BankInfo;
import model.Command;
import model.CurrentAccount;
import model.FixedDepositAccount;
import model.SavingsAccount;
import model.WithdrawRule;

import util.CommandParser;
import util.StatementFormatter;
import util.Validator;

import static model.TransactionType.DEPOSIT;

public class MiniBank {

    enum MenuOption {
        OPEN_ACCOUNT,
        DEPOSIT,
        WITHDRAW,
        TRANSFER,
        EXIT
    }

    public static void main(String[] args) {

        BankInfo bank = new BankInfo(
                "MiniBank",
                "Anand Branch"
        );

        Scanner s = new Scanner(System.in);
        boolean run = true;

        Account[] accounts = new Account[3];

        accounts[0] = new SavingsAccount(
                "Ansh",
                5000,
                2000
        );

        accounts[1] = new CurrentAccount(
                "Priyanka",
                15000,
                3000
        );

        accounts[2] = new FixedDepositAccount(
                "Rahul",
                10000
        );

        System.out.println("\nAccount Interest Rates");

        for (Account account : accounts) {

            System.out.println(
                    account.getOwnerName()
                            + " : "
                            + account.interestRate()
                            + "%"
            );
        }

        if (accounts[0]
                instanceof SavingsAccount savings_account) {

            System.out.println(
                    "Savings Account found: "
                            + savings_account.getOwnerName()
            );
        }

        /*
         * Part B
         * InterestBearing interface
         */

        System.out.println("\nYearly Interest");

        for (Account account : accounts) {

            System.out.println(
                    account.getOwnerName()
                            + " : "
                            + account.yearlyInterest()
            );
        }

        /*
         * Part B
         * WithdrawRule using anonymous class
         */

        WithdrawRule anonymous_rule =
                new WithdrawRule() {

                    @Override
                    public boolean allow(
                            Account account,
                            long amount
                    ) {

                        return account.canWithdraw(amount);
                    }
                };

        System.out.println(
                "\nAnonymous Withdraw Rule: "
                        + anonymous_rule.allow(
                                accounts[0],
                                1000
                        )
        );

        /*
         * Part B
         * WithdrawRule using lambda
         */

        WithdrawRule lambda_rule =
                (account, amount) ->
                        account.canWithdraw(amount);

        System.out.println(
                "Lambda Withdraw Rule: "
                        + lambda_rule.allow(
                                accounts[1],
                                1000
                        )
        );

        /*
         * Validator Testing
         */

        System.out.println("\nValidator Testing");

        System.out.println(
                Validator.isValidMobile("9876543210")
        );

        System.out.println(
                Validator.isValidMobile("12345")
        );

        System.out.println(
                Validator.isValidEmail("abc@xyz.com")
        );

        System.out.println(
                Validator.isValidEmail("abcxyz.com")
        );

        System.out.println(
                Validator.isValidPan("ABCDE1234F")
        );

        System.out.println(
                Validator.isValidPan("ABC123")
        );

        System.out.println(
                Validator.isValidIfsc("SBIN0001234")
        );

        System.out.println(
                Validator.isValidIfsc("SBIN123")
        );

        System.out.println(
                Validator.isValidAmount("500")
        );

        System.out.println(
                Validator.isValidAmount("-500")
        );

        /*
         * Command Parser
         */

        Command command =
                CommandParser.parse(
                        "DEPOSIT AC0001 500"
                );

        /*
         * Static Import
         */

        if (command.type() == DEPOSIT) {

            System.out.println(
                    "\nStatic Import Used: DEPOSIT"
            );
        }

        System.out.println("\nParsed Command");

        System.out.println(
                "Type = "
                        + command.type()
        );

        System.out.println(
                "Account Number = "
                        + command.accountNumber()
        );

        System.out.println(
                "Amount = "
                        + command.amount()
        );

        /*
         * Account Statement
         */

        System.out.println("\nAccount Statement");

        System.out.println(
                StatementFormatter.buildStatement(
                        accounts[0]
                )
        );

        /*
         * Existing Menu
         */

        while (run) {

            System.out.println("\n--------------------");

            System.out.println(
                    bank.name()
                            + " - "
                            + bank.branch()
            );

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

                    option =
                            MenuOption.OPEN_ACCOUNT;

                    break;

                case 2:

                    option =
                            MenuOption.DEPOSIT;

                    break;

                case 3:

                    option =
                            MenuOption.WITHDRAW;

                    break;

                case 4:

                    option =
                            MenuOption.TRANSFER;

                    break;

                case 5:

                    option =
                            MenuOption.EXIT;

                    break;

                default:

                    option = null;
            }

            if (option == null) {

                System.out.println(
                        "Invalid Choice"
                );

                continue;
            }

            switch (option) {

                case OPEN_ACCOUNT -> {

                    System.out.println(
                            "\nAccounts Created Successfully"
                    );

                    System.out.println(
                            accounts[0]
                    );

                    System.out.println(
                            accounts[1]
                    );

                    System.out.println(
                            accounts[2]
                    );

                    System.out.println();

                    System.out.println(
                            "Account 1 equals Account 2 : "
                                    + accounts[0]
                                    .equals(accounts[1])
                    );

                    System.out.println(
                            "Account 1 equals Account 1 : "
                                    + accounts[0]
                                    .equals(accounts[0])
                    );

                    Object obj = accounts[0];

                    if (obj instanceof Account) {

                        System.out.println(
                                "obj is an Account"
                        );
                    }
                }

                case DEPOSIT -> {

                    System.out.println(
                            "Select Account"
                    );

                    System.out.println(
                            "1."
                                    + accounts[0]
                                    .getOwnerName()
                    );

                    System.out.println(
                            "2."
                                    + accounts[1]
                                    .getOwnerName()
                    );

                    System.out.println(
                            "3."
                                    + accounts[2]
                                    .getOwnerName()
                    );

                    int acc = s.nextInt();

                    System.out.print(
                            "Enter Amount:"
                    );

                    long amount =
                            s.nextLong();

                    switch (acc) {

                        case 1:

                            accounts[0]
                                    .deposit(amount);

                            System.out.println(
                                    "Balance ="
                                            + accounts[0]
                                            .getBalance()
                            );

                            break;

                        case 2:

                            accounts[1]
                                    .deposit(amount);

                            System.out.println(
                                    "Balance ="
                                            + accounts[1]
                                            .getBalance()
                            );

                            break;

                        case 3:

                            accounts[2]
                                    .deposit(amount);

                            System.out.println(
                                    "Balance ="
                                            + accounts[2]
                                            .getBalance()
                            );

                            break;

                        default:

                            System.out.println(
                                    "Invalid Account"
                            );
                    }
                }

                case WITHDRAW -> {

                    System.out.println(
                            "Select Account"
                    );

                    System.out.println(
                            "1. "
                                    + accounts[0]
                                    .getOwnerName()
                    );

                    System.out.println(
                            "2. "
                                    + accounts[1]
                                    .getOwnerName()
                    );

                    System.out.println(
                            "3. "
                                    + accounts[2]
                                    .getOwnerName()
                    );

                    int acc = s.nextInt();

                    System.out.print(
                            "Enter Amount:"
                    );

                    long amount =
                            s.nextLong();

                    switch (acc) {

                        case 1:

                            if (accounts[0]
                                    .withdraw(amount)) {

                                System.out.println(
                                        "Withdrawal Successful"
                                );

                            } else {

                                System.out.println(
                                        "Insufficient Balance"
                                );
                            }

                            System.out.println(
                                    "Balance ="
                                            + accounts[0]
                                            .getBalance()
                            );

                            break;

                        case 2:

                            if (accounts[1]
                                    .withdraw(amount)) {

                                System.out.println(
                                        "Withdrawal Successful"
                                );

                            } else {

                                System.out.println(
                                        "Insufficient Balance"
                                );
                            }

                            System.out.println(
                                    "Balance ="
                                            + accounts[1]
                                            .getBalance()
                            );

                            break;

                        case 3:

                            if (accounts[2]
                                    .withdraw(amount)) {

                                System.out.println(
                                        "Withdrawal Successful"
                                );

                            } else {

                                System.out.println(
                                        "Insufficient Balance"
                                );
                            }

                            System.out.println(
                                    "Balance ="
                                            + accounts[2]
                                            .getBalance()
                            );

                            break;

                        default:

                            System.out.println(
                                    "Invalid Account"
                            );
                    }
                }

                case TRANSFER -> {

                    System.out.println(
                            "Transfer feature will be implemented later"
                    );
                }

                case EXIT -> {

                    System.out.println(
                            "Thank you for using MiniBank."
                    );

                    run = false;
                }
            }
        }

        s.close();
    }
}