/********************
Last names: Bolaños, Gendernalik, Mallari, Roca
Language: C 
Paradigm(s): Procedural
********************/

#define __USE_MINGW_ANSI_STDIO 1

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <math.h>
#include <ctype.h>
#include <errno.h>

typedef struct
{
    int y;
    int m;
    int d;
} Date;

typedef struct
{
    char name[128];
    char id[11];
    char currency[8];
    char type[16];
    Date date;
    int seconds;
    double amount;
    double balance;
    long row;
} Tx;

typedef struct
{
    char name[128];
    char id[11];
    Date opened;
    double balance;
    double adb;
    double interest;
    double dep;
    double wd;
    int age;
    int nd;
    int nw;
} Account;

Tx *tx = NULL;

size_t nt = 0;

size_t capacity = 0;

Account *accounts = NULL;

size_t na = 0;

const char *path = "bank_transactions.csv";

const char *codes[] = {"PHP", "USD", "JPY", "GBP", "EUR", "CNY"};

double rates[6] = {1, 0, 0, 0, 0, 0};

unsigned long long highest = 0;

int loaded = 0;

void die(const char *s)
{
    perror(s);
    exit(1);
}

int leap(int y)
{
    return ((y % 4) == 0) && (((y % 100) != 0) || ((y % 400) == 0));
}

int daysMonth(int y, int m)
{
    const int days[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    return days[m - 1] + ((m == 2) && leap(y));
}

/* Convert a calendar date into a day count, without DST effects. */

long dayNumber(Date d)
{
    long n = 0;
    int y;
    int m;
    for (y = 1900; y < d.y; y++)
    {
        n += 365 + leap(y);
    }

    for (m = 1; m < d.m; m++)
    {
        n += daysMonth(d.y, m);
    }

    return (n + d.d) - 1;
}

int parseDate(const char *s, Date *d)
{
    int used = 0;
    if ((sscanf(s, "%d/%d/%d%n", &d->d, &d->m, &d->y, &used) != 3) || s[used])
    {
        return 0;
    }
    return (((((d->y >= 1900) && (d->y <= 9999)) && (d->m >= 1)) && (d->m <= 12)) && (d->d >= 1)) && (d->d <= daysMonth(d->y, d->m));
}

void dateText(Date d, char *s)
{
    sprintf(s, "%02d/%02d/%04d", d.d, d.m, d.y);
}

Date nowDate(int *seconds)
{
    time_t t = time(NULL);
    struct tm *p = localtime(&t);
    Date d;
    if (!p)
    {
        die("localtime");
    }
    d.y = p->tm_year + 1900;
    d.m = p->tm_mon + 1;
    d.d = p->tm_mday;
    *seconds = ((p->tm_hour * 3600) + (p->tm_min * 60)) + p->tm_sec;
    return d;
}

void *resize(void *p, size_t n)
{
    void *q = realloc(p, n);
    if (!q)
    {
        die("memory");
    }
    return q;
}

void addTx(Tx t)
{
    if (nt == capacity)
    {
        capacity = (capacity) ? (capacity * 2) : (1024);
        tx = resize(tx, capacity * (sizeof(*tx)));
    }
    tx[nt++] = t;
}

char *trim(char *s)
{
    char *e;
    while (isspace((unsigned char) (*s)))
    {
        s++;
    }

    e = s + strlen(s);
    while ((e > s) && isspace((unsigned char) e[-1]))
    {
        *(--e) = 0;
    }

    return s;
}

/* Read eight CSV fields, including quoted commas and escaped quotes. */

int splitCSV(char *s, char f[8][256])
{
    int i = 0;
    int k = 0;
    int quoted = 0;
    int closed = 0;
    memset(f, 0, 8 * 256);
    while (((*s) && ((*s) != '\r')) && ((*s) != '\n'))
    {
        char c = *(s++);
        if (quoted)
        {
            if (c == '"')
            {
                if ((*s) == '"')
                {
                    s++;
                }
                else
                {
                    quoted = 0;
                    closed = 1;
                    continue;
                }
            }
        }
        else
        {
            if (c == ',')
            {
                if ((++i) >= 8)
                {
                    return 0;
                }
                k = 0;
                closed = 0;
                continue;
            }
            if (((c == '"') && (k == 0)) && (!closed))
            {
                quoted = 1;
                continue;
            }
            if (closed || (c == '"'))
            {
                return 0;
            }
        }
        if (k >= 255)
        {
            return 0;
        }
        f[i][k++] = c;
    }

    return (!quoted) && (i == 7);
}

int number(const char *s, double *v)
{
    char clean[256];
    char *end;
    size_t i;
    size_t k = 0;
    for (i = 0; s[i]; i++)
    {
        if (s[i] != ',')
        {
            if (k >= 255)
            {
                return 0;
            }
            clean[k++] = s[i];
        }
    }

    clean[k] = 0;
    errno = 0;
    *v = strtod(clean, &end);
    return (((((end != clean) && (!(*trim(end)))) && (!errno)) && isfinite(*v)) && ((*v) >= 0)) && ((*v) <= 1e12);
}

int validId(const char *s)
{
    size_t i;
    if (strlen(s) != 10)
    {
        return 0;
    }
    for (i = 0; i < 10; i++)
    {
        if (!isdigit((unsigned char) s[i]))
        {
            return 0;
        }
    }

    return 1;
}

int cmpTx(const void *aa, const void *bb)
{
    const Tx *a = aa;
    const Tx *b = bb;
    int c = strcmp(a->id, b->id);
    long delta;
    if (c)
    {
        return c;
    }
    delta = dayNumber(a->date) - dayNumber(b->date);
    if (delta)
    {
        return (delta > 0) ? (1) : (-1);
    }
    if (a->seconds != b->seconds)
    {
        return (a->seconds > b->seconds) ? (1) : (-1);
    }
    return (a->row > b->row) - (a->row < b->row);
}

/* Load the transaction file and log invalid rows. */

int loadFile(void)
{
    FILE *in = fopen(path, "r");
    FILE *log;
    char line[4096];
    char fields[8][256];
    long rows = 0;
    long errors = 0;
    long foreign = 0;
    Tx t;
    int h;
    int m;
    int s;
    int n;
    int i;
    char *f[8];
    const char *header = "account_name,account_number,date,time,currency,amount,type,current_balance";
    if (!in)
    {
        perror(path);
        return 0;
    }
    if ((!fgets(line, sizeof(line), in)) || strcmp(trim(line), header))
    {
        fprintf(stderr, "CSV header must be: %s\n", header);
        fclose(in);
        return 0;
    }
    log = fopen("ingestion_errors.log", "w");
    if (!log)
    {
        fclose(in);
        perror("log");
        return 0;
    }
    nt = 0;
    highest = 0;
    while (fgets(line, sizeof(line), in))
    {
        int ok = 1;
        rows++;
        memset(&t, 0, sizeof(t));
        if ((!strchr(line, '\n')) && (!feof(in)))
        {
            int ch;
            while (((ch = fgetc(in)) != '\n') && (ch != EOF))
            {
            }

            ok = 0;
        }
        if (ok)
        {
            ok = splitCSV(line, fields);
        }
        if (ok)
        {
            for (i = 0; i < 8; i++)
            {
                f[i] = trim(fields[i]);
                if (!(*f[i]))
                {
                    ok = 0;
                }
            }

            if (validId(f[1]))
            {
                unsigned long long id = strtoull(f[1], NULL, 10);
                if (id > highest)
                {
                    highest = id;
                }
            }
            n = 0;
            if (((((((((((((((strlen(f[0]) >= (sizeof(t.name))) || (!validId(f[1]))) || (!parseDate(f[2], &t.date))) || (sscanf(f[3], "%d:%d:%d%n", &h, &m, &s, &n) != 3)) || f[3][n]) || (h < 0)) || (h > 23)) || (m < 0)) || (m > 59)) || (s < 0)) || (s > 59)) || (strlen(f[4]) != 3)) || (!number(f[5], &t.amount))) || (strcmp(f[6], "deposit") && strcmp(f[6], "withdrawal"))) || (!number(f[7], &t.balance)))
            {
                ok = 0;
            }
            if (ok)
            {
                strcpy(t.name, f[0]);
                strcpy(t.id, f[1]);
                strcpy(t.currency, f[4]);
                strcpy(t.type, f[6]);
                t.seconds = ((h * 3600) + (m * 60)) + s;
                t.row = rows;
            }
        }
        if (!ok)
        {
            errors++;
            fprintf(log, "CSV line %ld: invalid/missing field, date, time, numeric value, type or CSV format\n", rows + 1);
        }
        else
        {
            if (strcmp(t.currency, "PHP"))
            {
                foreign++;
            }
            else
            {
                addTx(t);
            }
        }
    }

    if (ferror(in))
    {
        fclose(in);
        fclose(log);
        die("read CSV");
    }
    fclose(in);
    fclose(log);
    if (nt > 1)
    {
        qsort(tx, nt, sizeof(*tx), cmpTx);
    }
    loaded = 1;
    printf("Rows: %ld; valid PHP: %zu; non-PHP excluded: %ld; invalid: %ld\n", rows, nt, foreign, errors);
    printf("Validation log: ingestion_errors.log\n");
    return 1;
}

void csvText(FILE *f, const char *s)
{
    fputc('"', f);
    while (*s)
    {
        if ((*s) == '"')
        {
            fputc('"', f);
        }
        fputc(*(s++), f);
    }

    fputc('"', f);
}

void money(FILE *f, double v)
{
    char raw[96];
    size_t i;
    size_t n;
    size_t start = 0;
    snprintf(raw, sizeof(raw), "%.2f", v);
    n = strcspn(raw, ".");
    fputc('"', f);
    if (raw[0] == '-')
    {
        fputc('-', f);
        start = 1;
    }
    for (i = start; i < n; i++)
    {
        fputc(raw[i], f);
        if (((((n - i) - 1) % 3) == 0) && ((i + 1) < n))
        {
            fputc(',', f);
        }
    }

    fputs(raw + n, f);
    fputc('"', f);
}

FILE *output(const char *name)
{
    FILE *f = fopen(name, "w");
    if (!f)
    {
        die(name);
    }
    return f;
}

void closeOutput(FILE *f)
{
    int error = ferror(f);
    if ((fclose(f) != 0) || error)
    {
        die("write output");
    }
}

int byADB(const void *aa, const void *bb)
{
    const Account *a = aa;
    const Account *b = bb;
    if (a->adb != b->adb)
    {
        return (a->adb > b->adb) ? (-1) : (1);
    }
    return strcmp(a->id, b->id);
}

int byInterest(const void *aa, const void *bb)
{
    const Account *a = aa;
    const Account *b = bb;
    if (a->interest != b->interest)
    {
        return (a->interest > b->interest) ? (-1) : (1);
    }
    return strcmp(a->id, b->id);
}

int byFlow(const void *aa, const void *bb)
{
    const Account *a = aa;
    const Account *b = bb;
    double x = a->dep - a->wd;
    double y = b->dep - b->wd;
    if (x != y)
    {
        return (x > y) ? (-1) : (1);
    }
    return strcmp(a->id, b->id);
}

typedef struct
{
    char name[128];
    char id[11];
    Date date;
    double wd;
    double dep;
    double velocity;
    int nd;
    int risk;
    int undefined;
} Daily;

int byDaily(const void *aa, const void *bb)
{
    const Daily *a = aa;
    const Daily *b = bb;
    long d = dayNumber(a->date) - dayNumber(b->date);
    if (d)
    {
        return (d > 0) ? (1) : (-1);
    }
    if (a->risk != b->risk)
    {
        return b->risk - a->risk;
    }
    return strcmp(a->id, b->id);
}

void preview(const char *file)
{
    FILE *f = fopen(file, "r");
    char line[2048];
    int i;
    if (!f)
    {
        return;
    }
    printf("\n%s (first 2 rows):\n", file);
    for (i = 0; (i < 3) && fgets(line, sizeof(line), f); i++)
    {
        fputs(line, stdout);
    }

    fclose(f);
}

/* Calculate and export the four reports and summary. */

void reports(Date asof)
{
    Date first = {asof.y, asof.m, 1};
    Date prev = first;
    size_t i;
    size_t j;
    size_t k;
    size_t nd = 0;
    long cutoff = dayNumber(asof);
    long begin;
    long end;
    long d;
    double totalDep = 0;
    double totalWd = 0;
    double totalBalance = 0;
    double totalInterest = 0;
    Daily *daily = resize(NULL, (nt + 1) * (sizeof(*daily)));
    FILE *f;
    if ((--prev.m) == 0)
    {
        prev.m = 12;
        prev.y--;
    }
    begin = dayNumber(prev);
    end = dayNumber(first);
    na = 0;
    accounts = resize(accounts, (nt + 1) * (sizeof(*accounts)));
    for (i = 0; i < nt; i = j)
    {
        Account a;
        double balance = 0;
        double sum = 0;
        size_t cursor;
        memset(&a, 0, sizeof(a));
        j = i + 1;
        while ((j < nt) && (!strcmp(tx[i].id, tx[j].id)))
        {
            j++;
        }

        if (dayNumber(tx[i].date) > cutoff)
        {
            continue;
        }
        strcpy(a.id, tx[i].id);
        strcpy(a.name, tx[i].name);
        a.opened = tx[i].date;
        a.age = (int) (cutoff - dayNumber(a.opened));
        for (k = i; (k < j) && (dayNumber(tx[k].date) <= cutoff); k++)
        {
            a.balance = tx[k].balance;
            strcpy(a.name, tx[k].name);
            if (!strcmp(tx[k].type, "deposit"))
            {
                totalDep += tx[k].amount;
            }
            else
            {
                totalWd += tx[k].amount;
            }
            if ((dayNumber(tx[k].date) >= begin) && (dayNumber(tx[k].date) < end))
            {
                if (!strcmp(tx[k].type, "deposit"))
                {
                    a.dep += tx[k].amount;
                    a.nd++;
                }
                else
                {
                    a.wd += tx[k].amount;
                    a.nw++;
                }
            }
            if (((nd == 0) || strcmp(daily[nd - 1].id, a.id)) || (dayNumber(daily[nd - 1].date) != dayNumber(tx[k].date)))
            {
                memset(&daily[nd], 0, sizeof(*daily));
                strcpy(daily[nd].id, a.id);
                strcpy(daily[nd].name, tx[k].name);
                daily[nd].date = tx[k].date;
                nd++;
            }
            if (!strcmp(tx[k].type, "deposit"))
            {
                daily[nd - 1].dep += tx[k].amount;
                daily[nd - 1].nd++;
            }
            else
            {
                daily[nd - 1].wd += tx[k].amount;
            }
        }

        cursor = i;
        for (d = begin; d < end; d++)
        {
            while ((cursor < j) && (dayNumber(tx[cursor].date) <= d))
            {
                balance = tx[cursor++].balance;
            }

            sum += balance;
        }

        a.adb = sum / ((double) (end - begin));
        a.interest = a.balance * (pow(1 + (0.05 / 365), 30) - 1);
        accounts[na++] = a;
        totalBalance += a.balance;
        if (a.balance >= 10000)
        {
            totalInterest += a.interest;
        }
    }

    qsort(accounts, na, sizeof(*accounts), byADB);
    f = output("report1_banking_privilege_tiers.csv");
    fputs("AccountName,AccountAge,CurrentBalance,AverageDailyBalance,PrivilegeTier\n", f);
    for (i = 0; i < na; i++)
    {
        if (accounts[i].adb >= 100000)
        {
            Account *a = &accounts[i];
            const char *tier = "Silver";
            if (a->adb >= 1000000)
            {
                tier = "Platinum";
            }
            else
            {
                if (a->adb >= 500000)
                {
                    tier = "Gold";
                }
            }
            csvText(f, a->name);
            fprintf(f, ",%d,", a->age);
            money(f, a->balance);
            fputc(',', f);
            money(f, a->adb);
            fprintf(f, ",%s\n", tier);
        }
    }

    closeOutput(f);
    qsort(accounts, na, sizeof(*accounts), byInterest);
    f = output("report2_interest_expense_projection.csv");
    fputs("AccountName,CurrentBalance,ExpectedInterest\n", f);
    for (i = 0; i < na; i++)
    {
        if (accounts[i].balance >= 10000)
        {
            csvText(f, accounts[i].name);
            fputc(',', f);
            money(f, accounts[i].balance);
            fputc(',', f);
            money(f, accounts[i].interest);
            fputc('\n', f);
        }
    }

    closeOutput(f);
    for (i = 0; i < nd; i++)
    {
        daily[i].undefined = (daily[i].dep == 0) && (daily[i].wd > 0);
        if (daily[i].dep > 0)
        {
            daily[i].velocity = (daily[i].wd / daily[i].dep) * 100;
        }
        daily[i].risk = ((daily[i].wd >= 500000) || daily[i].undefined) || (daily[i].velocity >= 90);
    }

    qsort(daily, nd, sizeof(*daily), byDaily);
    f = output("report3_suspicious_client_behavior.csv");
    fputs("AccountName,Date,TotalWithdrawalAmounts,NumberOfDeposits,TotalDepositAmounts,TurnoverVelocity (%),RiskFlagLevel\n", f);
    for (i = 0; i < nd; i++)
    {
        char date[32];
        Daily *a = &daily[i];
        dateText(a->date, date);
        csvText(f, a->name);
        fprintf(f, ",%s,", date);
        money(f, a->wd);
        fprintf(f, ",%d,", a->nd);
        money(f, a->dep);
        fputc(',', f);
        if (!a->undefined)
        {
            money(f, a->velocity);
        }
        fprintf(f, ",%s\n", (a->risk) ? ("Suspicious") : ("Normal"));
    }

    closeOutput(f);
    free(daily);
    qsort(accounts, na, sizeof(*accounts), byFlow);
    f = output("report4_monthly_net_cash_flow.csv");
    fputs("AccountName,NumberOfDeposits,NumberOfWithdrawals,TotalDeposits,TotalWithdrawals,NetCashFlow\n", f);
    for (i = 0; i < na; i++)
    {
        Account *a = &accounts[i];
        csvText(f, a->name);
        fprintf(f, ",%d,%d,", a->nd, a->nw);
        money(f, a->dep);
        fputc(',', f);
        money(f, a->wd);
        fputc(',', f);
        money(f, a->dep - a->wd);
        fputc('\n', f);
    }

    closeOutput(f);
    f = output("summary.json");
    {
        char date[32];
        dateText(asof, date);
        fprintf(f, "{\n  \"as_of_date\": \"%s\",\n  \"total_accounts\": %zu,\n  \"total_deposits\": %.2f,\n  \"total_withdrawals\": %.2f,\n  \"total_balance\": %.2f,\n  \"total_interest_expense\": %.2f\n}\n", date, na, totalDep, totalWd, totalBalance, totalInterest);
    }
    closeOutput(f);
    printf("\nPrevious calendar month: %02d/%04d. Reports saved.\n", prev.m, prev.y);
    preview("report1_banking_privilege_tiers.csv");
    preview("report2_interest_expense_projection.csv");
    preview("report3_suspicious_client_behavior.csv");
    preview("report4_monthly_net_cash_flow.csv");
    preview("summary.json");
}

void input(const char *prompt, char *s, int cap)
{
    printf("%s", prompt);
    fflush(stdout);
    if (!fgets(s, cap, stdin))
    {
        free(tx);
        free(accounts);
        exit(0);
    }
    if (!strchr(s, '\n'))
    {
        int ch;
        while (((ch = getchar()) != '\n') && (ch != EOF))
        {
        }

        s[0] = 0;
    }
    s[strcspn(s, "\r\n")] = 0;
}

double readValue(const char *prompt, double min, double max, int integer)
{
    char s[256];
    double v;
    while (1)
    {
        input(prompt, s, sizeof(s));
        if (((number(s, &v) && (v >= min)) && (v <= max)) && ((!integer) || (floor(v) == v)))
        {
            return v;
        }
        printf("Enter %s between %.2f and %.2f.\n", (integer) ? ("a whole number") : ("a number"), min, max);
    }

}

int yes(const char *prompt)
{
    char s[16];
    while (1)
    {
        input(prompt, s, sizeof(s));
        if ((!strcmp(s, "Y")) || (!strcmp(s, "y")))
        {
            return 1;
        }
        if ((!strcmp(s, "N")) || (!strcmp(s, "n")))
        {
            return 0;
        }
        puts("Enter Y or N.");
    }

}

int currency(void)
{
    int i;
    for (i = 0; i < 6; i++)
    {
        printf("[%d] %s\n", i + 1, codes[i]);
    }

    return ((int) readValue("Currency: ", 1, 6, 1)) - 1;
}

int rateReady(int i)
{
    if (rates[i] > 0)
    {
        return 1;
    }
    puts("Record this exchange rate with option 5 first.");
    return 0;
}

int append(Tx t)
{
    FILE *f = fopen(path, "ab+");
    char date[32];
    long length;
    int last = '\n';
    int ok;
    if (!f)
    {
        perror(path);
        return 0;
    }
    if (fseek(f, 0, SEEK_END))
    {
        fclose(f);
        return 0;
    }
    length = ftell(f);
    if (length > 0)
    {
        if (fseek(f, -1, SEEK_END))
        {
            fclose(f);
            return 0;
        }
        last = fgetc(f);
    }
    if (fseek(f, 0, SEEK_END))
    {
        fclose(f);
        return 0;
    }
    if (length == 0)
    {
        fputs("account_name,account_number,date,time,currency,amount,type,current_balance\n", f);
    }
    else
    {
        if (last != '\n')
        {
            fputc('\n', f);
        }
    }
    dateText(t.date, date);
    csvText(f, t.name);
    fprintf(f, ",%s,%s,%02d:%02d:%02d,PHP,%.2f,%s,%.2f\n", t.id, date, t.seconds / 3600, (t.seconds / 60) % 60, t.seconds % 60, t.amount, t.type, t.balance);
    ok = !ferror(f);
    if (fclose(f))
    {
        ok = 0;
    }
    if (!ok)
    {
        puts("Write failed. Check CSV before retrying.");
        return 0;
    }
    return loadFile();
}

/* Register an account and save its initial deposit. */

void registerAccount(void)
{
    Tx t;
    char name[128];
    memset(&t, 0, sizeof(t));
    do
    {
        int words = 0;
        int inside = 0;
        size_t i;
        input("Account Name (First name Last name): ", name, sizeof(name));
        for (i = 0; name[i]; i++)
        {
            if (isspace((unsigned char) name[i]))
            {
                inside = 0;
            }
            else
            {
                if (!inside)
                {
                    words++;
                    inside = 1;
                }
            }
        }

        if (words < 2)
        {
            puts("Enter at least a first and last name.");
            continue;
        }
        if (highest >= 9999999999ULL)
        {
            puts("No more 10-digit account numbers available.");
            return;
        }
        strcpy(t.name, trim(name));
        if (highest < 999999999ULL)
        {
            highest = 999999999ULL;
        }
        unsigned long long nextNumber = highest + 1;

    unsigned long firstFive =
        (unsigned long)(nextNumber / 100000ULL);

    unsigned long lastFive =
        (unsigned long)(nextNumber % 100000ULL);

    snprintf(t.id, sizeof(t.id), "%05lu%05lu", firstFive, lastFive);
        t.amount = readValue("Initial Deposit (PHP, minimum 5000): ", 5000, 1e9, 0);
        t.amount = round(t.amount * 100) / 100;
        t.balance = t.amount;
        t.date = nowDate(&t.seconds);
        strcpy(t.currency, "PHP");
        strcpy(t.type, "deposit");
        if (append(t))
        {
            printf("Account created successfully! Account Number: %s\n", t.id);
        }
        return;
    }
    while (1);
}

int selectAccount(Tx *latest)
{
    char id[32];
    size_t i;
    int found = 0;
    input("Account Number (10 digits): ", id, sizeof(id));
    for (i = 0; i < nt; i++)
    {
        if (!strcmp(tx[i].id, id))
        {
            *latest = tx[i];
            found = 1;
        }
    }

    if (!found)
    {
        puts("No PHP account found for that number.");
        return 0;
    }
    printf("Account Name: %s\nCurrent Balance: %.2f PHP\n", latest->name, latest->balance);
    return 1;
}

/* Deposit or withdraw, then save the updated balance. */

void transaction(int deposit)
{
    Tx t;
    int c;
    int sec;
    double amount;
    Date today;
    if (!selectAccount(&t))
    {
        return;
    }
    today = nowDate(&sec);
    if ((dayNumber(t.date) > dayNumber(today)) || ((dayNumber(t.date) == dayNumber(today)) && (t.seconds > sec)))
    {
        puts("Account has a future-dated transaction. Cannot insert an earlier balance update.");
        return;
    }
    c = currency();
    if (!rateReady(c))
    {
        return;
    }
    amount = readValue((deposit) ? ("Deposit Amount: ") : ("Withdraw Amount: "), 0.01, 1e9, 0);
    t.amount = round((amount * rates[c]) * 100) / 100;
    if ((t.amount < 0.01) || (t.amount > 1e12))
    {
        puts("Converted amount out of range.");
        return;
    }
    if ((!deposit) && (t.amount > t.balance))
    {
        puts("Insufficient funds.");
        return;
    }
    if (deposit)
    {
        t.balance += t.amount;
    }
    else
    {
        t.balance -= t.amount;
    }
    t.balance = round(t.balance * 100) / 100;
    if (t.balance > 1e12)
    {
        puts("Balance exceeds supported range.");
        return;
    }
    t.date = today;
    t.seconds = sec;
    strcpy(t.type, (deposit) ? ("deposit") : ("withdrawal"));
    if (append(t))
    {
        printf("PHP amount: %.2f\nUpdated Balance: %.2f PHP\n", t.amount, t.balance);
    }
}

void exchange(void)
{
    int source;
    int target;
    double amount;
    do
    {
        puts("Source Currency:");
        source = currency();
        if (!rateReady(source))
        {
            return;
        }
        amount = readValue("Source Amount: ", 0.01, 1e9, 0);
        puts("Target Currency:");
        target = currency();
        if (!rateReady(target))
        {
            return;
        }
        printf("Exchange Amount: %.2f %s\n", (amount * rates[source]) / rates[target], codes[target]);
    }
    while (yes("Convert another currency (Y/N): "));
}

void recordRate(void)
{
    int i;
    int c = currency();
    FILE *f;
    if (c == 0)
    {
        puts("PHP base rate stays 1.00.");
        return;
    }
    rates[c] = readValue("PHP per one unit: ", 0.000001, 1e6, 0);
    f = fopen("exchange_rates.csv", "w");
    if (!f)
    {
        perror("rates file");
        return;
    }
    for (i = 0; i < 6; i++)
    {
        fprintf(f, "%s,%.8f\n", codes[i], rates[i]);
    }

    closeOutput(f);
    puts("Exchange rates saved.");
}

/* Display projected interest without changing the account balance. */

void interestPreview(void)
{
    Tx a;
    int days;
    int d;
    double b;
    double v;
    if (!selectAccount(&a))
    {
        return;
    }
    days = (int) readValue("Total Number of Days: ", 1, 36500, 1);
    b = a.balance;
    puts("Day | Interest | Balance (PHP)");
    for (d = 1; d <= days; d++)
    {
        v = (b * 0.05) / 365;
        b += v;
        printf("%d | %.2f | %.2f\n", d, v, b);
    }

    puts("Projection only; the CSV balance is unchanged.");
}

Date reportDate(void)
{
    char s[64];
    Date d;
    int sec;
    while (1)
    {
        input("As-of date dd/mm/yyyy (Enter = today): ", s, sizeof(s));
        if (!(*s))
        {
            return nowDate(&sec);
        }
        if (parseDate(s, &d) && (d.y > 1900))
        {
            return d;
        }
        puts("Enter a valid date after 1900.");
    }

}

int main(int argc, char **argv)
{
    int choice;
    int sec;
    Date asof;
    FILE *f;
    if ((argc > 1) && (!strcmp(argv[1], "--reports")))
    {
        asof = nowDate(&sec);
        if ((argc > 2) && ((!parseDate(argv[2], &asof)) || (asof.y <= 1900)))
        {
            fprintf(stderr, "Use --reports dd/mm/yyyy [CSV path]\n");
            return 1;
        }
        if (argc > 3)
        {
            path = argv[3];
        }
        if (!loadFile())
        {
            return 1;
        }
        reports(asof);
        free(tx);
        free(accounts);
        return 0;
    }
    if (argc > 1)
    {
        path = argv[1];
    }
    f = fopen("exchange_rates.csv", "r");
    if (f)
    {
        char code[8];
        double rate;
        int i;
        while (fscanf(f, "%7[^,],%lf\n", code, &rate) == 2)
        {
            for (i = 1; i < 6; i++)
            {
                if ((((!strcmp(code, codes[i])) && isfinite(rate)) && (rate > 0)) && (rate <= 1e6))
                {
                    rates[i] = rate;
                }
            }

        }

        fclose(f);
    }
    if (!loadFile())
    {
        puts("Place the supplied bank_transactions.csv beside the program and restart.");
        return 1;
    }
    do
    {
        puts("\nSelect Transaction:\n[1] Register Account\n[2] Deposit Amount\n[3] Withdraw Amount\n[4] Currency Exchange\n[5] Record Exchange Rates\n[6] Show Interest Amount\n[7] Generate Reports\n[0] Exit");
        choice = (int) readValue("Choice: ", 0, 7, 1);
        switch (choice)
        {
            case 1:
                do
            {
                registerAccount();
            }
            while (yes("Register another account (Y/N): "));
                break;

            case 2:
                do
            {
                transaction(1);
            }
            while (yes("Deposit another amount (Y/N): "));
                break;

            case 3:
                do
            {
                transaction(0);
            }
            while (yes("Withdraw another amount (Y/N): "));
                break;

            case 4:
                exchange();
                break;

            case 5:
                recordRate();
                break;

            case 6:
                interestPreview();
                break;

            case 7:
            {
                int r;
                do
                {
                    puts("[1] Load the file\n[2] Generate Reports\n[0] Back");
                    r = (int) readValue("Choice: ", 0, 2, 1);
                    if (r == 1)
                    {
                        loadFile();
                    }
                    if ((r == 2) && loaded)
                    {
                        reports(reportDate());
                    }
                }
                while (r != 0);
                break;
            }

            case 0:
                break;

        }

    }
    while (choice != 0);
    free(tx);
    free(accounts);
    puts("Goodbye!");
    return 0;
}

