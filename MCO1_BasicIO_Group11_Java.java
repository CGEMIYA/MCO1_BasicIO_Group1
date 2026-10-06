/********************
Last names: Gendernalik, Mallari, Roca, Salas
Language: Java
Paradigm(s): Object-Oriented Programming
********************/

import java.util.Scanner;

public class MCO1_BasicIO_Group11_Java {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        MainMenu(); 
        RegisterAccount(); 
        DepositAmount();
        WithdrawAmount(); 
        RecordExchange();
        CurrencyExchange();
    } 
    
    public static void MainMenu() {
        int choice;

        System.out.println("Select Transaction:"); 
        System.out.println("[1] Register Account Name"); 
        System.out.println("[2] Deposit Amount"); 
        System.out.println("[3] Withdraw Amount"); 
        System.out.println("[4] Currency Exchange"); 
        System.out.println("[5] Record Exchange Rates"); 
        System.out.println("[6] Show Interest Amount\n");

        System.out.print("Choice: ");
        choice = scanner.nextInt();
        scanner.nextLine();

        System.out.println("\n***");
        System.out.println("Choice = " + choice + "\n");
    }

    public static void RegisterAccount() {
        String account;
        
        System.out.println("Register Account Name");

        System.out.print("Account Name: ");
        account = scanner.nextLine();

        System.out.println("\n***");
        System.out.println("Account Name = " + account + "\n");
    }

    public static void DepositAmount() {
        String account;
        float amount;
        
        System.out.println("Deposit Amount");

        System.out.print("Account Name: ");
        account = scanner.nextLine();

        System.out.println("Current Balance: 1000.00");
        System.out.println("Currency: PHP");

        System.out.print("\nDeposit Amount: ");
        amount = scanner.nextFloat();
        scanner.nextLine();      

        System.out.println("\n***");
        System.out.println("Account Name = " + account);
        System.out.printf("Deposit Amount = %.2f\n\n", amount);
    }

    public static void WithdrawAmount() {
        String account;
        float amount;
        
        System.out.println("Withdraw Amount");

        System.out.print("Account Name: ");
        account = scanner.nextLine();

        System.out.println("Current Balance: 1000.00");
        System.out.println("Currency: PHP");

        System.out.print("\nWithdraw Amount: ");
        amount = scanner.nextFloat();
        scanner.nextLine();      

        System.out.println("\n***");
        System.out.println("Account Name = " + account);
        System.out.printf("Withdraw Amount = %.2f\n\n", amount);
    }

    public static void RecordExchange() {
        String choice;
        float rate;
    
        System.out.println("Record Exchange Rate\n"); 
        System.out.println("[1] Philippine Peso (PHP)"); 
        System.out.println("[2] United States Dollar (USD)"); 
        System.out.println("[3] Japanese Yen (JPY)"); 
        System.out.println("[4] British Pound Sterling (GBP)"); 
        System.out.println("[5] Euro (EUR)"); 
        System.out.println("[6] Chinese Yuan Renminni (CNY)\n");

        System.out.print("Select Foreign Currency: ");
        choice = scanner.nextLine();

        System.out.print("Exchange Rate: ");
        rate = scanner.nextFloat();
        scanner.nextLine();

        System.out.println("\n***");
        System.out.printf("Select Foreign Currency = %s\n", choice);
        System.out.printf("Exchange Rate = %.2f\n\n", rate);
    }

    public static void CurrencyExchange() {
        float amount;

        System.out.println("Foreign Currency Exchange");
        System.out.print("Source Amount: ");
        amount = scanner.nextFloat();
        scanner.nextLine();

        System.out.println("\nExchanged Currency");
        System.out.printf("[1] Philippine Peso (PHP) = %.2f\n", amount); 
        System.out.printf("[2] United States Dollar (USD) = %.2f\n", amount * 62); 
        System.out.printf("[3] Japanese Yen (JPY) = %.2f\n", amount * 0.40); 
        System.out.printf("[4] British Pound Sterling (GBP) = %.2f\n", amount * 84); 
        System.out.printf("[5] Euro (EUR) = %.2f\n", amount * 72); 
        System.out.printf("[6] Chinese Yuan Renminni (CNY) = %.2f\n", amount * 9);

        System.out.println("\n***");
        System.out.println("Target Currency = Philippine Peso (PHP)");
        System.out.printf("Source Amount (PHP) = %.2f", amount);
    }
}