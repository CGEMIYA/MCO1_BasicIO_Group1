/********************
Last names: Bolanos, Gendernalik, Mallari, Roca
Language: Java
Paradigm(s): Object-Oriented Programming
********************/

import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;


public class MP_11_Java {
    private static Scanner scanner = new Scanner(System.in);
    private static Map<String, Account> Accounts = new HashMap<>();
    private static Map<String, Float> Currencies = new HashMap<>();
    
    //Starts the program
    public static void main(String[] args) {
        InitializeCurrencies();
        MainMenu(); 
    } 
    
    //Sets up the initial currency conversion rates
    public static void InitializeCurrencies() {
        Currencies.put("PHP", 1.00f);
        Currencies.put("USD", 62.00f);
        Currencies.put("JPY", 0.40f);
        Currencies.put("GBP", 84.00f);
        Currencies.put("EUR", 72.00f);
        Currencies.put("CNY", 9.00f);
    }

    //Shows the main menu and handles user choices
    public static void MainMenu() {
        int choice = 0;
        while(choice != 7) {
            System.out.println("Select Transaction:"); 
            System.out.println("[1] Register Account Name"); 
            System.out.println("[2] Deposit Amount"); 
            System.out.println("[3] Withdraw Amount"); 
            System.out.println("[4] Currency Exchange"); 
            System.out.println("[5] Record Exchange Rates"); 
            System.out.println("[6] Show Interest Amount");
            System.out.println("[7] Exit Program\n");

            try {
                System.out.print("Choice: ");
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (java.util.InputMismatchException e) {
                System.out.println("Invalid Choice. Please try again.\n");
                scanner.next();
                choice = 0;
                continue;
            }

            switch(choice) {
                case 1: RegisterAccount(); choice = BackToMainMenu(); break;
                case 2: DepositAmount(); choice = BackToMainMenu(); break;
                case 3: WithdrawAmount(); choice = BackToMainMenu(); break;
                case 4: CurrencyExchange(); choice = BackToMainMenu(); break;
                case 5: RecordExchange(); choice = BackToMainMenu(); break;
                case 6: ShowInterestAmount(); choice = BackToMainMenu(); break;
                case 7: System.out.println("Exiting Program.\n"); break;
                default: System.out.println("Invalid Choice. Please try again.\n");
            }
        }
    }

    //Creates and saves a new account
    public static void RegisterAccount() {
        String AccountName;
        System.out.println("Register Account Name");
        System.out.print("Account Name: ");
        AccountName = scanner.nextLine();

        if (Accounts.containsKey(AccountName))
            System.out.println("\nAccount Already Exists.\n");
        
        else {
            Account NewAccount = new Account(AccountName, 0);
            Accounts.put(NewAccount.GetAccountName(), NewAccount);
        }
    }

    //Adds money to an existing account
    public static void DepositAmount() {
        String AccountName;
        Account CurrentAccount;
        String CurrencyName;
        float ConversionRate;
        float DepositAmount = -9999;
        float ConvertedAmount;
        
        System.out.println("Deposit Amount");

        System.out.print("Account Name: ");
        AccountName = scanner.nextLine();

        if (!Accounts.containsKey(AccountName)) {
            System.out.println("\nAccount Does not Exists.\n");
            return;
        }

        else {
            CurrentAccount = Accounts.get(AccountName);
            System.out.printf("\nCurrent Balance: %.2f\n", CurrentAccount.GetBalance());
            System.out.print("Currency: ");
            CurrencyName = scanner.nextLine();

            if(!Currencies.containsKey(CurrencyName))
                System.out.println("\nUnknown Currency.\n");

            else
            {
                while(DepositAmount == -9999) {
                        try {
                            System.out.print("\nDeposit Amount (or type -1 to cancel): ");
                            DepositAmount = scanner.nextFloat();
                            scanner.nextLine();
                        } catch (java.util.InputMismatchException e) {
                            System.out.println("Invalid Deposit Amount. Please try again.\n");
                            scanner.next();
                            continue;
                            }      
                    
                    if (DepositAmount == -1) {
                        System.out.println("Cancelling Transaction.\n");
                        return;
                    }

                    if (DepositAmount < 0)
                        System.out.println("Invalid Deposit Amount. Cannot Deposit Negative Value.\n");       

                    else {
                        ConversionRate = Currencies.get(CurrencyName);
                        ConvertedAmount = DepositAmount * ConversionRate;

                        System.out.printf("Deposit Amount = %.2f\n", ConvertedAmount);
                        CurrentAccount.SetBalance(ConvertedAmount + CurrentAccount.GetBalance());
                        System.out.printf("Updated Balance = %.2f\n", CurrentAccount.GetBalance());
                        break;
                    }
                }
            }
        }
    }

    //Removes money from an account if the balance is enough
    public static void WithdrawAmount() {
        String AccountName;
        Account CurrentAccount;
        String CurrencyName;
        float ConversionRate;
        float WithdrawAmount = -9999;
        float ConvertedAmount;
        
        System.out.println("Withdraw Amount");

        System.out.print("Account Name: ");
        AccountName = scanner.nextLine();

        if (!Accounts.containsKey(AccountName)) {
            System.out.println("\nAccount Does not Exists.\n");
            return;
        }

        else {
            CurrentAccount = Accounts.get(AccountName);
            System.out.printf("\nCurrent Balance: %.2f\n", CurrentAccount.GetBalance());
            System.out.print("Currency: ");
            CurrencyName = scanner.nextLine();

            if(!Currencies.containsKey(CurrencyName))
                System.out.println("\nUnknown Currency.\n");

            else
            {
                while(WithdrawAmount == -9999) {
                        try {
                            System.out.print("\nWithdraw Amount (or type -1 to cancel): ");
                            WithdrawAmount = scanner.nextFloat();
                            scanner.nextLine();
                        } catch (java.util.InputMismatchException e) {
                            System.out.println("Invalid Withdraw Amount. Please try again.\n");
                            scanner.next();
                            continue;
                            }      
                    
                    if (WithdrawAmount == -1) {
                        System.out.println("Cancelling Transaction.\n");
                        return;
                    }

                    if (WithdrawAmount < 0)
                        System.out.println("Invalid Withdraw Amount. Cannot Withdraw Negative Value.\n");       

                    ConversionRate = Currencies.get(CurrencyName);
                    ConvertedAmount = WithdrawAmount * ConversionRate;

                    if (ConvertedAmount > CurrentAccount.GetBalance())
                        System.out.println("Cannot withdraw more than current balance.\n");

                    else {
                        System.out.printf("Withdraw Amount = %.2f\n", ConvertedAmount);
                        CurrentAccount.SetBalance(CurrentAccount.GetBalance() - ConvertedAmount);
                        System.out.printf("Updated Balance = %.2f\n", CurrentAccount.GetBalance());
                        break;
                    }
                }
            }
        }
    }

    //Converts money from one currency to another
    public static void CurrencyExchange() {
        char ChoiceYesNo = 'y';
        int SourceChoice;
        float SourceAmount;
        int TargetChoice = 0;
        String SourceCurr;
        String TargetCurr;
        float SourceRate;
        float TargetRate;
        float ExchangeAmount;
        String YesNoInput;

        while (ChoiceYesNo == 'y' || ChoiceYesNo == 'Y') {
            System.out.println("Foreign Currency Exchange");
            System.out.println("Source Currency Option:");
            System.out.println("[1] Philippine Peso (PHP)");
            System.out.println("[2] United States Dollar (USD)");
            System.out.println("[3] Japanese Yen (JPY)");
            System.out.println("[4] British Pound Sterling (GBP)");
            System.out.println("[5] Euro (EUR)");
            System.out.println("[6] Chinese Yuan Renminni (CNY)\n");

            SourceChoice = 0;
            try {
                System.out.print("Source Currency: ");
                SourceChoice = scanner.nextInt();
                scanner.nextLine();
            } catch (java.util.InputMismatchException e) {
                System.out.println("Invalid choice. Please try again.\n");
                scanner.next();
                continue;
            }

            if (SourceChoice < 1 || SourceChoice > 6) {
                System.out.println("Invalid Source Currency option. Please try again.\n");
                continue;
            }

            SourceAmount = 0;
            try {
                System.out.print("Source Amount: ");
                SourceAmount = scanner.nextFloat();
                scanner.nextLine();
            } catch (java.util.InputMismatchException e) {
                System.out.println("Invalid Amount. Please try again.\n");
                scanner.next();
                continue;
            }

            System.out.println("\nExchanged Currency Options:");
            System.out.println("[1] Philippine Peso (PHP)");
            System.out.println("[2] United States Dollar (USD)");
            System.out.println("[3] Japanese Yen (JPY)");
            System.out.println("[4] British Pound Sterling (GBP)");
            System.out.println("[5] Euro (EUR)");
            System.out.println("[6] Chinese Yuan Renminni (CNY)\n");

            TargetChoice = 0;
            try {
                System.out.print("Exchange Currency: ");
                TargetChoice = scanner.nextInt();
                scanner.nextLine();
            } catch (java.util.InputMismatchException e) {
                System.out.println("Invalid choice. Please try again.\n");
                scanner.next();
                continue;
            }

            if (TargetChoice < 1 || TargetChoice > 6) {
                System.out.println("Invalid Exchange Currency option. Please try again.\n");
                continue;
            }

            SourceCurr = "";
            switch (SourceChoice) {
                case 1: SourceCurr = "PHP"; break;
                case 2: SourceCurr = "USD"; break;
                case 3: SourceCurr = "JPY"; break;
                case 4: SourceCurr = "GBP"; break;
                case 5: SourceCurr = "EUR"; break;
                case 6: SourceCurr = "CNY"; break;
            }

            TargetCurr = "";
            switch (TargetChoice) {
                case 1: TargetCurr = "PHP"; break;
                case 2: TargetCurr = "USD"; break;
                case 3: TargetCurr = "JPY"; break;
                case 4: TargetCurr = "GBP"; break;
                case 5: TargetCurr = "EUR"; break;
                case 6: TargetCurr = "CNY"; break;
            }

            SourceRate = Currencies.get(SourceCurr);
            TargetRate = Currencies.get(TargetCurr);

            ExchangeAmount = (SourceAmount * SourceRate) / TargetRate;

            System.out.printf("Exchange Amount: %.2f\n", ExchangeAmount);
            while (true) {
                        System.out.print("Convert another currency (Y/N)? ");
                        YesNoInput = scanner.nextLine().trim();

                        if (YesNoInput.equalsIgnoreCase("y")) {
                            ChoiceYesNo = 'y';
                            break;
                        } 
                        else if (YesNoInput.equalsIgnoreCase("n")) {
                            ChoiceYesNo = 'n';
                            break;
                        } 
                        else {
                            System.out.println("Invalid choice. Please enter Y or N.\n");
                        }
                    }
        }
    }

    //Updates the exchange rate for a currency
    public static void RecordExchange() {
        int choice = 0;
        String input;
        String CurrencyName;
        float rate;

        while (choice != 7) {
            System.out.println("Record Exchange Rate\n"); 
            System.out.println("[1] Philippine Peso (PHP)"); 
            System.out.println("[2] United States Dollar (USD)"); 
            System.out.println("[3] Japanese Yen (JPY)"); 
            System.out.println("[4] British Pound Sterling (GBP)"); 
            System.out.println("[5] Euro (EUR)"); 
            System.out.println("[6] Chinese Yuan Renminbi (CNY)");

            System.out.print("Select Foreign Currency: ");
            input = scanner.nextLine().trim();
            input = input.replace("[", "").replace("]", "").trim();

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid Choice. Please try again.\n");
                choice = 0;
                continue;
            }

            if (choice >= 1 && choice <= 6) {
                CurrencyName = "";
                switch (choice) {
                    case 1: CurrencyName = "PHP"; break;
                    case 2: CurrencyName = "USD"; break;
                    case 3: CurrencyName = "JPY"; break;
                    case 4: CurrencyName = "GBP"; break;
                    case 5: CurrencyName = "EUR"; break;
                    case 6: CurrencyName = "CNY"; break;
                }

                rate = -1;
                while (true) {
                    try {
                        System.out.print("Exchange Rate: ");
                        rate = scanner.nextFloat();
                        scanner.nextLine();
                        
                        if (rate < 0) {
                            System.out.println("Exchange rate cannot be negative. Please try again.\n");
                            continue;
                        }
                        break;
                    } catch (java.util.InputMismatchException e) {
                        System.out.println("Invalid Exchange Rate. Please try again.\n");
                        scanner.next();
                    }
                }
                Currencies.put(CurrencyName, rate);
                break;
            } 
            else {
                System.out.println("Invalid Choice. Please try again.\n");
            }
        }
    }

    //Calculates and displays daily interest for an account over time
    public static void ShowInterestAmount() {
        String AccountName;
        Account CurrentAccount;
        String CurrencyName;
        int TotalDays = 0;
        float ConversionRate;
        float CurrentBalanceInCurrency;
        float AnnualInterestRate;
        float Balance;
        float DailyInterest;

        System.out.println("Show Interest Amount");
        System.out.print("Account Name: ");
        AccountName = scanner.nextLine();

        if (!Accounts.containsKey(AccountName)) {
            System.out.println("\nAccount Does not Exist.\n");
            return;
        }

        CurrentAccount = Accounts.get(AccountName);
        System.out.print("Currency: ");
        CurrencyName = scanner.nextLine();

        if (!Currencies.containsKey(CurrencyName)) {
            System.out.println("\nUnknown Currency.\n");
            return;
        }

        ConversionRate = Currencies.get(CurrencyName);
        CurrentBalanceInCurrency = CurrentAccount.GetBalance() / ConversionRate;
        System.out.printf("Current Balance: %.2f\n", CurrentBalanceInCurrency);
        System.out.println("Currency: " + CurrencyName);
        System.out.println("Interest Rate: 5%");

        try {
            System.out.print("Total Number of Days: ");
            TotalDays = scanner.nextInt();
            scanner.nextLine();
        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid number of days. Please try again.\n");
            scanner.next();
            return;
        }

        if (TotalDays <= 0) {
            System.out.println("Number of days must be greater than zero.\n");
            return;
        }

        System.out.println("\nTotal Number of Days: " + TotalDays + " days\n");
        System.out.println("Day | Interest | Balance |");
        AnnualInterestRate = 0.05f;
        Balance = CurrentBalanceInCurrency;

        for (int day = 1; day <= TotalDays; day++) {
            DailyInterest = Balance * (AnnualInterestRate / 365.0f);
            Balance += DailyInterest;
            
            System.out.printf("%-3d | %-8.2f | %.2f |\n", day, DailyInterest, Balance);
        }
    }

    //Asks the user if they want to return to the main menu
    public static int BackToMainMenu() {
        String YesNoInput;     
        while (true) {
            System.out.print("\nBack to the Main Menu? (Y/N)? ");
            YesNoInput = scanner.nextLine().trim();

            if (YesNoInput.equalsIgnoreCase("y"))
                return 0;
            else if (YesNoInput.equalsIgnoreCase("n"))
                return 7;
            else
                System.out.println("Invalid choice. Please enter Y or N.\n");
        }
    }
}