/*
********************
Last names: Bolaños, Gendernalik, Mallari, Roca
Language: Kotlin
Paradigm(s): Imperative / Procedural
********************
*/
import kotlin.math.ceil

data class Account(
    val name: String,
    var balance: Double = 0.0,
    var currency: String = "PHP"
)

object ExchangeRates {
    var php: Double = 1.00
    var usd: Double = 52.00 // Expressed as PHP value per rate unit as per sample output
    var yen: Double = 0.38
    var pound: Double = 68.00
    var eur: Double = 58.00
    var yuan: Double = 7.50
}

fun mainMenuReturn(): Int {
    println("\nBack to the Main Menu (Y/N): ")
    val choose: String = readln()
    return if (choose.equals("N", ignoreCase = true)) 0 else 1
}

fun main() {
    val accounts = mutableMapOf<String, Account?>()
    var close = 1
    while (close == 1) {
        val choice = mainMenu()
        when (choice) {
            1 -> registerMenu(accounts)
            2 -> depositMenu(accounts)
            3 -> withdrawMenu(accounts)
            4 -> currencyExchangeMenu()
            5 -> recordExchangeRatesMenu()
            6 -> showInterestAmountMenu(accounts)
            else -> println("\nInvalid Option!")
        }
        close = mainMenuReturn()
    }
}

fun mainMenu(): Int {
    println("\nSelect Transaction:")
    println("[1] Register Account Name")
    println("[2] Deposit Amount")
    println("[3] Withdraw Amount")
    println("[4] Currency Exchange")
    println("[5] Record Exchange Rates")
    println("[6] Show Interest Amount")
    print("\nChoice: ")
    return readln().toIntOrNull() ?: 0
}

fun registerMenu(accounts: MutableMap<String, Account?>) {
    println("\nRegister Account Name")
    print("Account Name: ")
    val getAccountName = readln()
    if (accounts.containsKey(getAccountName)) {
        println("\nAccount already exists\n")
        return
    } else {
        val newAccount = Account(getAccountName, 0.0)
        accounts[getAccountName] = newAccount
    }
}

fun depositMenu(accounts: MutableMap<String, Account?>) {
    println("\nDeposit Amount")
    print("Account Name: ")
    val name = readln()

    if (!accounts.containsKey(name)) {
        println("\nAccount doesn't exist\n")
        return
    }

    val account = accounts[name]!!

    println("Current Balance: %.2f".format(account.balance))
    println("Currency: ${account.currency}")

    print("\nDeposit Amount: ")
    val amount = readln().toDoubleOrNull() ?: 0.0

    if (amount <= 0.0) {
        println("\nInvalid deposit amount\n")
        return
    }

    account.balance += amount
    println("Updated Balance: %.2f".format(account.balance))
}

fun withdrawMenu(accounts: MutableMap<String, Account?>) {
    println("\nWithdraw Amount")
    print("Account Name: ")
    val name = readln()

    if (!accounts.containsKey(name)) {
        println("\nAccount doesn't exist\n")
        return
    }

    val account = accounts[name]!!
    println("Current Balance: %.2f".format(account.balance))
    println("Currency: ${account.currency}")

    print("\nWithdraw Amount: ")
    val amount = readln().toDoubleOrNull() ?: 0.0

    if (amount > account.balance) {
        println("\nInsufficient Funds\n")
        return
    } else if (amount <= 0.0) {
        println("\nInvalid Withdrawal Amount\n")
        return
    }

    account.balance -= amount
    println("Updated Balance: %.2f".format(account.balance))
}

fun currencyExchangeMenu() {
    var c = "y"
    while (c.equals("y", ignoreCase = true)) {
        println("\nForeign Currency Exchange")

        val currencyMenu = """
            [1] Philippine Peso (PHP)
            [2] United States Dollar (USD)
            [3] Japanese Yen (JPY)
            [4] British Pound Sterling (GBP)
            [5] Euro (EUR)
            [6] Chinese Yuan Renminbi (CNY)
        """.trimIndent()

        println("Source Currency Option:")
        println(currencyMenu)
        print("\nSource Currency: ")
        val sourceChoice = readln().toIntOrNull()

        print("Source Amount: ")
        val sourceAmount = readln().toDoubleOrNull() ?: 0.0

        println("\nExchanged Currency Options:")
        println(currencyMenu)
        print("\nExchange Currency: ")
        val targetChoice = readln().toIntOrNull()

        val sourceRate = when (sourceChoice) {
            1 -> ExchangeRates.php
            2 -> ExchangeRates.usd
            3 -> ExchangeRates.yen
            4 -> ExchangeRates.pound
            5 -> ExchangeRates.eur
            6 -> ExchangeRates.yuan
            else -> null
        }

        val targetRate = when (targetChoice) {
            1 -> ExchangeRates.php
            2 -> ExchangeRates.usd
            3 -> ExchangeRates.yen
            4 -> ExchangeRates.pound
            5 -> ExchangeRates.eur
            6 -> ExchangeRates.yuan
            else -> null
        }

        if (sourceRate == null || targetRate == null || sourceRate == 0.0) {
            println("Invalid currency selection.")
            return
        }

        // Base currency conversion logic (PHP as base)
        val exchangedAmount = (sourceAmount * sourceRate) / targetRate

        println("Exchange Amount: %.2f".format(exchangedAmount))

        print("Convert another currency (Y/N)? . . . ")
        c = readln()
    }
}

fun recordExchangeRatesMenu() {
    println("\nRecord Exchange Rate\n")
    println("[1] Philippine Peso (PHP)")
    println("[2] United States Dollar (USD)")
    println("[3] Japanese Yen (JPY)")
    println("[4] British Pound Sterling (GBP)")
    println("[5] Euro (EUR)")
    println("[6] Chinese Yuan Renminbi (CNY)\n")

    print("Select Foreign Currency: ")
    val choice = readln().toIntOrNull()

    print("Exchange Rate: ")
    val rate = readln().toDoubleOrNull() ?: 0.0

    when (choice) {
        1 -> ExchangeRates.php = rate
        2 -> ExchangeRates.usd = rate
        3 -> ExchangeRates.yen = rate
        4 -> ExchangeRates.pound = rate
        5 -> ExchangeRates.eur = rate
        6 -> ExchangeRates.yuan = rate
        else -> println("Invalid selection.")
    }
}

fun showInterestAmountMenu(accounts: MutableMap<String, Account?>) {
    println("\nShow Interest Amount")
    print("Account Name: ")
    val name = readln()

    if (!accounts.containsKey(name)) {
        println("\nAccount doesn't exist\n")
        return
    }

    val account = accounts[name]!!

    print("Interest Rate (%): ")
    val rate = readln().toDoubleOrNull() ?: 0.0

    print("Total Number of Days: ")
    val days = readln().toIntOrNull() ?: 0

    println("\nShow Interest Amount")
    println("Account Name: ${account.name}")
    println("Current Balance: %.2f".format(account.balance))
    println("Currency: ${account.currency}")
    println("Interest Rate: ${rate.toInt()}%")
    println("Total Number of Days: $days days\n")

    println("%-4s | %-8s | %-8s |".format("Day", "Interest", "Balance"))

    val rawDailyInterest = (account.balance * (rate / 100.0)) / 365.0
    val dailyInterest = ceil(rawDailyInterest * 100.0) / 100.0
    var balance = account.balance

    for (i in 1..days) {
        balance += dailyInterest
        println("%-4d | %-8.2f | %-8.2f |".format(i, dailyInterest, balance))
    }
}
