import java.util.*;
public class MiniBank{
    enum MenuOption{
        OPEN_ACCOUNT,
        DEPOSIT,WITHDRAW,TRANSFER,EXIT
    }
    record BankInfo(String name, String branch){

    }
    public static void main(String[] args){
        BankInfo bank=new BankInfo("MiniBank","Anand branch");

        System.out.println("--------------------");
        Scanner s=new Scanner(System.in);
        boolean run=true;

        while(run){
            System.out.println("\t"+"MiniBank Menu");
            System.out.println("\t"+"1) Open Account");
            System.out.println("\t"+"2) Deposit");
            System.out.println("\t"+"3) Withdraw");
            System.out.println("\t"+"4) Transfer");
            System.out.println("\t"+"5) Exit");
            System.out.print("Enter your choice:");
            int choice=s.nextInt();

            MenuOption option;
            switch(choice){
                case 1:
                    option=MenuOption.OPEN_ACCOUNT;
                    break;
                case 2:
                    option=MenuOption.DEPOSIT;
                    break;
                case 3:
                    option=MenuOption.WITHDRAW;
                    break;
                case 4:
                    option=MenuOption.TRANSFER;
                    break;
                case 5:
                    option=MenuOption.EXIT;
                    break;
                default:
                    option=null;
            }
            if(option==null){
                System.out.println("Invalid choice!");
            }
            switch (option) {

                case OPEN_ACCOUNT ->
                        System.out.println("Open Account - To be implemented in a later lab.");

                case DEPOSIT ->
                        System.out.println("Deposit - To be implemented in a later lab.");

                case WITHDRAW ->
                        System.out.println("Withdraw - To be implemented in a later lab.");

                case TRANSFER ->
                        System.out.println("Transfer - To be implemented in a later lab.");

                case EXIT -> {
                    System.out.println("Thank you for using MiniBank.");
                    run=false;
                }

            }




        }
    }
    

}