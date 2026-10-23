/********************
Last names: Bolanos, Gendernalik, Mallari, Roca
Language: C
Paradigm(s): Procedural
********************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>

#define ANNUAL_RATE 0.05
#define LIMIT 1000000000.0

typedef struct
{
    char name[100];
    double balance;
    int registered;
} Account;

const char *CURRENCIES[] = {
    "PHP", "USD", "JPY", "GBP", "EUR", "CNY"
};

/* PHP per one unit of currency.
   Zero means the foreign rate has not been recorded. */
double rates[] = {1.0, 0.0, 0.0, 0.0, 0.0, 0.0};


/* Read a line, including spaces. */
void readLine(const char *prompt, char *text, int size)
{
    int ch;

    printf("%s", prompt);
    fflush(stdout);

    if (fgets(text, size, stdin) == NULL)
    {
        printf("\nInput ended. Goodbye!\n");
        exit(0);
    }

    if (strchr(text, '\n') == NULL)
    {
        while ((ch = getchar()) != '\n' && ch != EOF)
        {
        }

        text[0] = '\0';
        printf("Input too long. Please use fewer characters.\n");
    }

    text[strcspn(text, "\r\n")] = '\0';
}


/* Read a valid number within the specified range.
   Set whole to 1 when an integer is required. */
double readNumber(const char *prompt,
                  double min,
                  double max,
                  int whole)
{
    char line[128];
    char *end;
    double value;

    while (1)
    {
        readLine(prompt, line, sizeof(line));
        value = strtod(line, &end);

        if (end == line)
        {
            printf("Please enter a number.\n");
            continue;
        }

        while (isspace((unsigned char)*end))
            end++;

        if (*end != '\0' ||
            !isfinite(value) ||
            value < min ||
            value > max ||
            (whole && floor(value) != value))
        {
            printf("Enter %s from %.2f to %.2f.\n",
                   whole ? "a whole number" : "a number",
                   min, max);
            continue;
        }

        return value;
    }
}


/* Accept only Y or N, ignoring letter case. */
int askYesNo(const char *prompt)
{
    char answer[20];

    while (1)
    {
        readLine(prompt, answer, sizeof(answer));

        if (strlen(answer) == 1)
        {
            if (toupper((unsigned char)answer[0]) == 'Y')
                return 1;

            if (toupper((unsigned char)answer[0]) == 'N')
                return 0;
        }

        printf("Please enter Y or N.\n");
    }
}


/* Display currencies and return their array index. */
int selectCurrency(const char *prompt)
{
    int i;

    for (i = 0; i < 6; i++)
        printf("[%d] %s\n", i + 1, CURRENCIES[i]);

    return (int)readNumber(prompt, 1, 6, 1) - 1;
}


/* Check whether the selected currency has a recorded rate. */
int hasRate(int currency)
{
    if (rates[currency] <= 0)
    {
        printf("Record the %s exchange rate using option 5 first.\n",
               CURRENCIES[currency]);
        return 0;
    }

    return 1;
}


/* Round a monetary value to two decimal places. */
double cents(double amount)
{
    return round(amount * 100.0) / 100.0;
}


/* Require registration before banking transactions. */
int requireAccount(const Account *account)
{
    if (!account->registered)
    {
        printf("Register an account using option 1 first.\n");
        return 0;
    }

    return 1;
}


void registerAccount(Account *account)
{
    char name[100];
    int i;
    int hasText;

    printf("\nRegister Account Name\n");

    if (account->registered)
    {
        printf("An account already exists: %s\n", account->name);
        return;
    }

    do
    {
        readLine("Account Name: ", name, sizeof(name));

        hasText = 0;

        for (i = 0; name[i] != '\0'; i++)
        {
            if (!isspace((unsigned char)name[i]))
                hasText = 1;
        }

        if (!hasText)
            printf("Account name cannot be blank.\n");

    } while (!hasText);

    strcpy(account->name, name);
    account->balance = 0.0;
    account->registered = 1;

    printf("Registered: %s\n", account->name);
    printf("Current Balance: %.2f PHP\n", account->balance);
}


/* deposit = 1 for deposits, 0 for withdrawals. */
void bankTransaction(Account *account, int deposit)
{
    int currency;
    double amount;
    double phpAmount;

    printf("\n%s Amount\n", deposit ? "Deposit" : "Withdraw");

    if (!requireAccount(account))
        return;

    printf("Account Name: %s\n", account->name);
    printf("Current Balance: %.2f\n", account->balance);
    printf("Currency: PHP\n");

    currency = selectCurrency("Transaction Currency: ");

    if (!hasRate(currency))
        return;

    amount = readNumber(
        deposit ? "Deposit Amount: " : "Withdraw Amount: ",
        0.01, LIMIT, 0
    );

    phpAmount = cents(amount * rates[currency]);

    if (phpAmount <= 0 || phpAmount > LIMIT)
    {
        printf("PHP equivalent must be between 0.01 and %.2f.\n",
               LIMIT);
        return;
    }

    if (deposit)
    {
        if (account->balance + phpAmount > LIMIT)
        {
            printf("Deposit exceeds the balance limit of %.2f PHP.\n",
                   LIMIT);
            return;
        }

        account->balance = cents(account->balance + phpAmount);
    }
    else
    {
        if (phpAmount > account->balance)
        {
            printf("Insufficient funds. Required: %.2f PHP.\n",
                   phpAmount);
            return;
        }

        account->balance = cents(account->balance - phpAmount);
    }

    printf("PHP equivalent: %.2f\n", phpAmount);
    printf("Updated Balance: %.2f PHP\n", account->balance);
}


void recordExchange(void)
{
    int currency;

    printf("\nRecord Exchange Rate\n");

    currency = selectCurrency("Select Foreign Currency: ");

    if (currency == 0)
    {
        printf("PHP is the base currency. Its rate stays at 1.00.\n");
        return;
    }

    rates[currency] = readNumber(
        "PHP per 1 unit of this currency: ",
        0.000001, 1000000.0, 0
    );

    printf("Recorded: 1 %s = %.6f PHP\n",
           CURRENCIES[currency], rates[currency]);
}


void currencyExchange(void)
{
    int source;
    int target;
    double amount;
    double result;

    do
    {
        printf("\nForeign Currency Exchange\n");
        printf("Source Currency Options:\n");

        source = selectCurrency("Source Currency: ");

        if (!hasRate(source))
            return;

        amount = readNumber("Source Amount: ", 0.01, LIMIT, 0);

        printf("\nExchanged Currency Options:\n");

        target = selectCurrency("Exchange Currency: ");

        if (!hasRate(target))
            return;

        /* Convert the source to PHP, then PHP to the target. */
        result = amount * rates[source] / rates[target];

        printf("Exchange Amount: %.2f %s\n",
               result, CURRENCIES[target]);

    } while (askYesNo("Convert another currency (Y/N): "));
}


void showInterest(const Account *account)
{
    int day;
    int days;
    double projectedBalance;
    double interest;

    printf("\nShow Interest Amount\n");

    if (!requireAccount(account))
        return;

    printf("Account Name: %s\n", account->name);
    printf("Current Balance: %.2f\n", account->balance);
    printf("Currency: PHP\n");
    printf("Interest Rate: 5%% per annum\n");

    days = (int)readNumber(
        "Total Number of Days: ",
        1, 36500, 1
    );

    projectedBalance = account->balance;

    printf("\nDay | Interest (PHP) | Balance (PHP)\n");

    for (day = 1; day <= days; day++)
    {
        interest = cents(
            projectedBalance * (ANNUAL_RATE / 365.0)
        );

        projectedBalance = cents(projectedBalance + interest);

        printf("%3d | %14.2f | %13.2f\n",
               day, interest, projectedBalance);
    }

    printf("Projection only. Actual balance remains %.2f PHP.\n",
           account->balance);
}


int main(void)
{
    Account account = {"", 0.0, 0};
    int choice;

    do
    {
        printf("\nSelect Transaction:\n");
        printf("[1] Register Account Name\n");
        printf("[2] Deposit Amount\n");
        printf("[3] Withdraw Amount\n");
        printf("[4] Currency Exchange\n");
        printf("[5] Record Exchange Rates\n");
        printf("[6] Show Interest Amount\n");
        printf("[0] Exit\n");

        choice = (int)readNumber("Choice: ", 0, 6, 1);

        switch (choice)
        {
            case 1:
                registerAccount(&account);
                break;

            case 2:
                bankTransaction(&account, 1);
                break;

            case 3:
                bankTransaction(&account, 0);
                break;

            case 4:
                currencyExchange();
                break;

            case 5:
                recordExchange();
                break;

            case 6:
                showInterest(&account);
                break;

            case 0:
                printf("Goodbye!\n");
                return 0;
        }

    } while (askYesNo("\nBack to the Main Menu (Y/N): "));

    printf("Goodbye!\n");
    return 0;
}
