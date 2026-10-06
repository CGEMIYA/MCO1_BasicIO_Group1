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
    if (scanf("%d", &choice) != 1)
        return;

    printf("\n***\n");
    printf("Choice = %d\n\n", choice);
}

void RegisterAccount(void)
{
    char accountName[100];

    printf("Register Account Name\n");
    printf("Account Name: ");
    if (scanf(" %99[^\n]", accountName) != 1)
        return;

    printf("\n***\n");
    printf("Account Name = %s\n\n", accountName);
}

void DepositAmount(void)
{
    char accountName[100];
    double amount;

    printf("Deposit Amount\n");

    printf("Account Name: ");
    if (scanf(" %99[^\n]", accountName) != 1)
        return;

    printf("Current Balance: 1000.00\n");
    printf("Currency: PHP\n");

    printf("\nDeposit Amount: ");
    if (scanf("%lf", &amount) != 1)
        return;

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
    if (scanf(" %99[^\n]", accountName) != 1)
        return;

    printf("Current Balance: 1000.00\n");
    printf("Currency: PHP\n");

    printf("\nWithdraw Amount: ");
    if (scanf("%lf", &amount) != 1)
        return;

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
    if (scanf("%d", &currencyChoice) != 1)
        return;

    printf("Exchange Rate: ");
    if (scanf("%lf", &exchangeRate) != 1)
        return;

    printf("\n***\n");
    printf("Select Foreign Currency = %d\n", currencyChoice);

    switch (currencyChoice)
    {
        case 1:
            printf("Foreign Currency = Philippine Peso (PHP)\n");
            break;
        case 2:
            printf("Foreign Currency = United States Dollar (USD)\n");
            break;
        case 3:
            printf("Foreign Currency = Japanese Yen (JPY)\n");
            break;
        case 4:
            printf("Foreign Currency = British Pound Sterling (GBP)\n");
            break;
        case 5:
            printf("Foreign Currency = Euro (EUR)\n");
            break;
        case 6:
            printf("Foreign Currency = Chinese Yuan Renminbi (CNY)\n");
            break;
        default:
            printf("Foreign Currency = Invalid choice\n");
            break;
    }

    printf("Exchange Rate = %.2f\n\n", exchangeRate);
}

void CurrencyExchange(void)
{
    double sourceAmount;

    printf("Foreign Currency Exchange\n");

    printf("Source Amount: ");
    if (scanf("%lf", &sourceAmount) != 1)
        return;

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