package service;

import exception.InvalidAmountException;
import model.Account;
import model.SavingsAccount;

public class AccountWorker implements Runnable {

    private final Account account;
    private final int times;
    private final long amount;

    public AccountWorker(Account account, int times, long amount) {
        this.account = account;
        this.times = times;
        this.amount = amount;
    }

    public static void main(String[] args) {
        Account sharedAccount = new SavingsAccount("Concurrent", 0, 0);
        Thread[] workers = new Thread[10];

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(
                    new AccountWorker(sharedAccount, 1000, 1),
                    "AccountWorker-" + (i + 1)
            );
            workers[i].start();
        }

        try {
            for (Thread worker : workers) {
                worker.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interrupted while waiting for account workers.");
            return;
        }

        System.out.println("Final balance = " + sharedAccount.getBalance());
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + " started.");

        for (int i = 0; i < times; i++) {
            try {
                account.deposit(amount);
            } catch (InvalidAmountException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        System.out.println(threadName + " finished.");
    }
}
