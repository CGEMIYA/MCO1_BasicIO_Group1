/********************
Last names: Bolanos, Gendernalik, Mallari, Roca
Language: C
Paradigm(s): Procedural
********************/

#include <stdio.h>

/* Function prototypes */
void MainMenu(void);
void RegisterAccount(void);
void DepositAmount(void);
void WithdrawAmount(void);
void RecordExchange(void);
void CurrencyExchange(void);

int main(void)
{
    MainMenu();
    RegisterAccount();
    DepositAmount();
    WithdrawAmount();
    RecordExchange();
    CurrencyExchange();

    return 0;
}

void MainMenu(void)
{
    int choice;

    printf("Select Transaction:\n");
    printf("[1] Register Account Name\n");
    printf("[2] Deposit Amount\n");
    printf("[3] Withdraw Amount\n");
    printf("[4] Currency Exchange\n");
    printf("[5] Record Exchange Rates\n");
    printf("[6] Show Interest Amount\n");

    printf("\nChoice: ");
    scanf("%d", &choice);

    printf("\n***\n");
    printf("Choice = %d\n\n", choice);
}

void RegisterAccount(void)
{
    char accountName[100];

    printf("Register Account Name\n");

    printf("Account Name: ");
    scanf(" %99[^\n]", accountName);

    printf("\n***\n");
    printf("Account Name = %s\n\n", accountName);
}

void DepositAmount(void)
{
    char accountName[100];
    double amount;

    printf("Deposit Amount\n");

    printf("Account Name: ");
    scanf(" %99[^\n]", accountName);

    printf("Current Balance: 1000.00\n");
    printf("Currency: PHP\n");

    printf("\nDeposit Amount: ");
    scanf("%lf", &amount);

    printf("\n***\n");
    printf("Account Name = %s\n", accountName);
    printf("Deposit Amount = %.2f\n\n", amount);
}

void WithdrawAmount(void)
{
    char accountName[100];
    double amount;

    printf("Withdraw Amount\n");

    printf("Account Name: ");
    scanf(" %99[^\n]", accountName);

    printf("Current Balance: 1000.00\n");
    printf("Currency: PHP\n");

    printf("\nWithdraw Amount: ");
    scanf("%lf", &amount);

    printf("\n***\n");
    printf("Account Name = %s\n", accountName);
    printf("Withdraw Amount = %.2f\n\n", amount);
}

void RecordExchange(void)
{
    int currencyChoice;
    double exchangeRate;

    printf("Record Exchange Rate\n\n");
    printf("[1] Philippine Peso (PHP)\n");
    printf("[2] United States Dollar (USD)\n");
    printf("[3] Japanese Yen (JPY)\n");
    printf("[4] British Pound Sterling (GBP)\n");
    printf("[5] Euro (EUR)\n");
    printf("[6] Chinese Yuan Renminbi (CNY)\n");

    printf("\nSelect Foreign Currency: ");
    scanf("%d", &currencyChoice);

    printf("Exchange Rate: ");
    scanf("%lf", &exchangeRate);

    printf("\n***\n");
    printf("Select Foreign Currency = %d\n", currencyChoice);
    printf("Exchange Rate = %.2f\n\n", exchangeRate);
}

void CurrencyExchange(void)
{
    double sourceAmount;

    printf("Foreign Currency Exchange\n");

    printf("Source Amount: ");
    scanf("%lf", &sourceAmount);

    printf("\nExchanged Currency\n");
    printf("[1] Philippine Peso (PHP) = %.2f\n", sourceAmount);
    printf("[2] United States Dollar (USD) = %.2f\n", sourceAmount * 62.00);
    printf("[3] Japanese Yen (JPY) = %.2f\n", sourceAmount * 0.40);
    printf("[4] British Pound Sterling (GBP) = %.2f\n", sourceAmount * 84.00);
    printf("[5] Euro (EUR) = %.2f\n", sourceAmount * 72.00);
    printf("[6] Chinese Yuan Renminbi (CNY) = %.2f\n", sourceAmount * 9.00);

    printf("\n***\n");
    printf("Target Currency = Philippine Peso (PHP)\n");
    printf("Source Amount (PHP) = %.2f\n", sourceAmount);
}