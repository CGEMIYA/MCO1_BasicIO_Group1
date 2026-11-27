/*
********************
Last names: Bolaños, Gendernalik, Mallari, Roca
Language: Kotlin
Paradigm(s): Imperative / Procedural + Functional Data Processing
********************
*/

import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

data class Account(
    val accountNumber: Long,
    val name: String,
    var balance: Double = 0.0,
    var currency: String = "PHP"
)

data class Transaction(
    val accountNumber: Long,
    val accountName: String,
    val date: LocalDate,
    val time: LocalTime,
    val type: String, // "deposit" or "withdrawal"
    val amount: Double,
    val currentBalance: Double,
    val currency: String
)

object ExchangeRates {
    var php: Double = 1.00
    var usd: Double = 52.00
    var yen: Double = 0.38
    var pound: Double = 68.00
    var eur: Double = 58.00
    var yuan: Double = 7.50
}

val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
const val CSV_FILE = "bank_transations.csv"

fun main() {
    val accounts = mutableMapOf<String, Account>()
    var loadedTransactions = listOf<Transaction>()

    // Pre-populate accounts from existing CSV if available
    syncAccountsFromCsv(accounts)

    var close = 1
    while (close == 1) {
        when (mainMenu()) {
            1 -> registerMenu(accounts)
            2 -> depositMenu(accounts)
            3 -> withdrawMenu(accounts)
            4 -> currencyExchangeMenu()
            5 -> recordExchangeRatesMenu()
            6 -> showInterestAmountMenu(accounts)
            7 -> loadedTransactions = generateReportsMenu(loadedTransactions)
            else -> println("\nInvalid Option!")
        }
        close = mainMenuReturn()
    }
}

fun mainMenu(): Int {
    println("\nSelect Transaction:")
    println("[1] Register Account")
    println("[2] Deposit Amount")
    println("[3] Withdraw Amount")
    println("[4] Currency Exchange")
    println("[5] Record Exchange Rates")
    println("[6] Show Interest Amount")
    println("[7] Generate Reports")
    print("\nChoice: ")
    return readln().toIntOrNull() ?: 0
}

fun mainMenuReturn(): Int {
    print("\nBack to the Main Menu (Y/N): ")
    val choose = readln()
    return if (choose.equals("N", ignoreCase = true)) 0 else 1
}

// ==========================================

fun ensureCsvHeader() {
    val file = File(CSV_FILE)
    if (!file.exists() || file.length() == 0L) {
        file.writeText("AccountNumber,AccountName,CurrentDate,CurrentTime,TransactionType,Amount,CurrentBalance,Currency\n")
    }
}

fun getNextAccountNumber(accounts: Map<String, Account>): Long {
    var maxAcc = 1000000000L - 1 // Ensures first account is 10-digit: 1000000000
    for (acc in accounts.values) {
        if (acc.accountNumber > maxAcc) maxAcc = acc.accountNumber
    }
    val file = File(CSV_FILE)
    if (file.exists()) {
        file.useLines { lines ->
            lines.drop(1).forEach { line ->
                val parts = parseCsvLine(line)
                val num = parts.getOrNull(0)?.toLongOrNull()
                if (num != null && num > maxAcc) maxAcc = num
            }
        }
    }
    return maxAcc + 1
}

fun appendTransactionToCsv(tx: Transaction) {
    ensureCsvHeader()
    val formattedAmount = "%.2f".format(tx.amount)
    val formattedBal = "%.2f".format(tx.currentBalance)
    val safeName = if (tx.accountName.contains(",")) "\"${tx.accountName}\"" else tx.accountName
    val row = "${tx.accountNumber},$safeName,${tx.date.format(DATE_FORMAT)},${tx.time.format(TIME_FORMAT)},${tx.type},$formattedAmount,$formattedBal,${tx.currency}\n"
    File(CSV_FILE).appendText(row)
}

fun syncAccountsFromCsv(accounts: MutableMap<String, Account>) {
    val file = File(CSV_FILE)
    if (!file.exists()) return
    file.useLines { lines ->
        lines.drop(1).forEach { line ->
            val parts = parseCsvLine(line)
            if (parts.size >= 7) {
                val accNum = parts[0].toLongOrNull() ?: return@forEach
                val name = parts[1].trim()
                val bal = parts[6].replace(",", "").toDoubleOrNull() ?: return@forEach
                val curr = parts.getOrNull(7)?.trim()?.ifEmpty { "PHP" } ?: "PHP"
                accounts[name] = Account(accNum, name, bal, curr)
            }
        }
    }
}

fun registerMenu(accounts: MutableMap<String, Account>) {
    var again = "Y"
    while (again.equals("Y", ignoreCase = true)) {
        println("\nRegister Account")
        print("Account Name: ")
        val name = readln().trim()

        if (name.isBlank()) {
            println("Account name cannot be empty.")
        } else if (accounts.containsKey(name)) {
            println("Account already exists!")
        } else {
            print("Initial Deposit: ")
            val initialDeposit = readln().toDoubleOrNull() ?: 0.0

            if (initialDeposit < 5000.00) {
                println("Minimum initial deposit is Php. 5,000.00.")
            } else {
                val accNum = getNextAccountNumber(accounts)
                val newAcc = Account(accNum, name, initialDeposit, "PHP")
                accounts[name] = newAcc

                val tx = Transaction(
                    accountNumber = accNum,
                    accountName = name,
                    date = LocalDate.now(),
                    time = LocalTime.now(),
                    type = "deposit",
                    amount = initialDeposit,
                    currentBalance = initialDeposit,
                    currency = "PHP"
                )
                appendTransactionToCsv(tx)
                println("Account created successfully! (Account #: $accNum)")
            }
        }
        print("Register another account? (Y/N): ")
        again = readln()
    }
}

fun depositMenu(accounts: MutableMap<String, Account>) {
    var again = "Y"
    while (again.equals("Y", ignoreCase = true)) {
        println("\nDeposit Amount")
        print("Account Name: ")
        val name = readln().trim()

        val account = accounts[name]
        if (account == null) {
            println("Account doesn't exist!")
        } else {
            println("Current Balance: %.2f".format(account.balance))
            println("Currency: ${account.currency}")
            print("Deposit Amount: ")
            val amount = readln().toDoubleOrNull() ?: 0.0

            if (amount <= 0.0) {
                println("Invalid deposit amount.")
            } else {
                account.balance += amount
                println("Updated Balance: %.2f".format(account.balance))

                val tx = Transaction(
                    accountNumber = account.accountNumber,
                    accountName = account.name,
                    date = LocalDate.now(),
                    time = LocalTime.now(),
                    type = "deposit",
                    amount = amount,
                    currentBalance = account.balance,
                    currency = account.currency
                )
                appendTransactionToCsv(tx)
            }
        }
        print("Deposit another amount? (Y/N): ")
        again = readln()
    }
}

fun withdrawMenu(accounts: MutableMap<String, Account>) {
    var again = "Y"
    while (again.equals("Y", ignoreCase = true)) {
        println("\nWithdraw Amount")
        print("Account Name: ")
        val name = readln().trim()

        val account = accounts[name]
        if (account == null) {
            println("Account doesn't exist!")
        } else {
            println("Current Balance: %.2f".format(account.balance))
            println("Currency: ${account.currency}")
            print("Withdraw Amount: ")
            val amount = readln().toDoubleOrNull() ?: 0.0

            if (amount > account.balance) {
                println("Insufficient Funds.")
            } else if (amount <= 0.0) {
                println("Invalid Withdrawal Amount.")
            } else {
                account.balance -= amount
                println("Updated Balance: %.2f".format(account.balance))

                val tx = Transaction(
                    accountNumber = account.accountNumber,
                    accountName = account.name,
                    date = LocalDate.now(),
                    time = LocalTime.now(),
                    type = "withdrawal",
                    amount = amount,
                    currentBalance = account.balance,
                    currency = account.currency
                )
                appendTransactionToCsv(tx)
            }
        }
        print("Withdraw another amount? (Y/N): ")
        again = readln()
    }
}

fun currencyExchangeMenu() {
    var c = "Y"
    while (c.equals("Y", ignoreCase = true)) {
        println("\nForeign Currency Exchange")
        val currencyMenu = """
            [1] Philippine Peso (PHP)
            [2] United States Dollar (USD)
            [3] Japanese Yen (JPY)
            [4] British Pound Sterling (GBP)
            [5] Euro (EUR)
            [6] Chinese Yuan Renminbi (CNY)
        """.trimIndent()

        println("Source Currency Option:\n$currencyMenu")
        print("\nSource Currency: ")
        val sourceChoice = readln().toIntOrNull()

        print("Source Amount: ")
        val sourceAmount = readln().toDoubleOrNull() ?: 0.0

        println("\nExchanged Currency Options:\n$currencyMenu")
        print("\nExchange Currency: ")
        val targetChoice = readln().toIntOrNull()

        val sourceRate = getRate(sourceChoice)
        val targetRate = getRate(targetChoice)

        if (sourceRate == null || targetRate == null || sourceRate <= 0.0 || targetRate <= 0.0) {
            println("Invalid currency selection or rate.")
            return
        }

        val exchangedAmount = (sourceAmount * sourceRate) / targetRate
        println("Exchange Amount: %.2f".format(exchangedAmount))

        print("Convert another currency (Y/N)? ")
        c = readln()
    }
}

fun getRate(choice: Int?): Double? = when (choice) {
    1 -> ExchangeRates.php
    2 -> ExchangeRates.usd
    3 -> ExchangeRates.yen
    4 -> ExchangeRates.pound
    5 -> ExchangeRates.eur
    6 -> ExchangeRates.yuan
    else -> null
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

    if (rate <= 0.0) {
        println("Exchange rate must be greater than 0.")
        return
    }

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

fun showInterestAmountMenu(accounts: MutableMap<String, Account>) {
    println("\nShow Interest Amount")
    print("Account Name: ")
    val name = readln().trim()

    val account = accounts[name]
    if (account == null) {
        println("\nAccount doesn't exist\n")
        return
    }

    print("Interest Rate (%): ")
    val rate = readln().toDoubleOrNull() ?: 0.0

    print("Total Number of Days: ")
    val days = readln().toIntOrNull() ?: 0

    println("\nShow Interest Amount")
    println("Account Name: ${account.name}")
    println("Current Balance: %.2f".format(account.balance))
    println("Currency: ${account.currency}")
    println("Interest Rate: %.2f%%".format(rate))
    println("Total Number of Days: $days days\n")

    println("%-4s | %-10s | %-12s |".format("Day", "Interest", "Balance"))

    val rawDailyInterest = (account.balance * (rate / 100.0)) / 365.0
    val dailyInterest = ceil(rawDailyInterest * 100.0) / 100.0
    var balance = account.balance

    for (i in 1..days) {
        balance += dailyInterest
        println("%-4d | %-10.2f | %-12.2f |".format(i, dailyInterest, balance))
    }
}

// ==========================================

fun generateReportsMenu(existingTransactions: List<Transaction>): List<Transaction> {
    var transactions = existingTransactions
    var stay = true

    while (stay) {
        println("\nGenerate Reports")
        println("[1] Load the file")
        println("[2] Generate Reports")
        println("[3] Back")
        print("Enter choice: ")

        when (readln().toIntOrNull()) {
            1 -> transactions = loadDataset()
            2 -> {
                if (transactions.isEmpty()) {
                    println("Dataset not loaded yet. Loading automatically...")
                    transactions = loadDataset()
                }
                if (transactions.isNotEmpty()) {
                    runAllReports(transactions)
                }
            }
            3 -> stay = false
            else -> println("Invalid choice.")
        }
    }
    return transactions
}

// Splits CSV lines safely even if AccountName is quoted (e.g., "Dela Cruz, Juan")
fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    for (ch in line) {
        when {
            ch == '"' -> inQuotes = !inQuotes
            ch == ',' && !inQuotes -> {
                result.add(current.toString().trim())
                current.clear()
            }
            else -> current.append(ch)
        }
    }
    result.add(current.toString().trim())
    return result
}

fun loadDataset(): List<Transaction> {
    val file = File(CSV_FILE)
    if (!file.exists()) {
        println("Error: $CSV_FILE not found.")
        return emptyList()
    }

    var totalRows = 0
    var parseErrors = 0
    val cleanedTransactions = mutableListOf<Transaction>()

    file.useLines { lines ->
        lines.drop(1).forEach { line ->
            if (line.isBlank()) return@forEach
            totalRows++
            val parts = parseCsvLine(line)

            if (parts.size < 7) {
                parseErrors++
                return@forEach
            }

            try {

                val currency = parts.getOrNull(7)?.uppercase()?.ifEmpty { "PHP" } ?: "PHP"
                if (currency != "PHP") return@forEach

                val accNum = parts[0].toLong()
                val accName = parts[1].ifBlank { throw IllegalArgumentException("Empty Name") }
                val date = LocalDate.parse(parts[2], DATE_FORMAT)
                val time = LocalTime.parse(parts[3], TIME_FORMAT)
                val type = parts[4].lowercase()
                if (type != "deposit" && type != "withdrawal") {
                    throw IllegalArgumentException("Invalid transaction type: $type")
                }

                val amount = parts[5].replace(",", "").toDouble()
                val balance = parts[6].replace(",", "").toDouble()

                cleanedTransactions.add(
                    Transaction(accNum, accName, date, time, type, amount, balance, currency)
                )
            } catch (e: Exception) {
                parseErrors++
            }
        }
    }

    println("Processing dataset... ($totalRows rows loaded, ${cleanedTransactions.size} filtered for PHP currency, $parseErrors parse errors skipped)")
    return cleanedTransactions
}


fun formatMoney(value: Double): String = "%,.2f".format(value)
fun escapeCsvField(value: String): String = if (value.contains(",")) "\"$value\"" else value

fun runAllReports(transactions: List<Transaction>) {
    println("Generating reports...")

    // Sort chronologically
    val sortedTx = transactions.sortedWith(compareBy({ it.date }, { it.time }))
    val latestDateInDataset = sortedTx.maxOf { it.date }
    val byAccount = sortedTx.groupBy { it.accountName }

    // ---------------------------------------------------------

    val lastMonth = YearMonth.from(latestDateInDataset).minusMonths(1)
    val daysInLastMonth = lastMonth.lengthOfMonth()

    data class Report1Row(
        val name: String,
        val ageDays: Long,
        val currentBalance: Double,
        val adb: Double,
        val tier: String
    )

    val report1Rows = byAccount.mapNotNull { (name, txs) ->
        val firstDate = txs.first().date
        val accountAge = ChronoUnit.DAYS.between(firstDate, latestDateInDataset)
        val latestBalance = txs.last().currentBalance

        val lastMonthTxs = txs.filter { YearMonth.from(it.date) == lastMonth }
        val eodByDay = lastMonthTxs.groupBy { it.date.dayOfMonth }
            .mapValues { (_, dayTxs) -> dayTxs.last().currentBalance }

        var runningBal = txs.lastOrNull { YearMonth.from(it.date) < lastMonth }?.currentBalance ?: 0.0
        var sumEod = 0.0
        for (day in 1..daysInLastMonth) {
            if (eodByDay.containsKey(day)) {
                runningBal = eodByDay[day]!!
            }
            sumEod += runningBal
        }

        val adb = sumEod / daysInLastMonth
        if (adb < 100_000.00) return@mapNotNull null

        val tier = when {
            adb < 500_000.00 -> "Silver"
            adb < 1_000_000.00 -> "Gold"
            else -> "Platinum"
        }
        Report1Row(name, accountAge, latestBalance, adb, tier)
    }.sortedByDescending { it.adb }

    val r1File = File("report1_banking_privilege_tiers.csv")
    r1File.printWriter().use { out ->
        out.println("AccountName,AccountAge,CurrentBalance,AverageDailyBalance,PrivilegeTier")
        report1Rows.forEach {
            out.println("${escapeCsvField(it.name)},${it.ageDays},\"${formatMoney(it.currentBalance)}\",\"${formatMoney(it.adb)}\",${it.tier}")
        }
    }

    // ---------------------------------------------------------

    data class Report2Row(
        val name: String,
        val currentBalance: Double,
        val expectedInterest: Double
    )

    val report2Rows = byAccount.mapNotNull { (name, txs) ->
        val latestBalance = txs.last().currentBalance
        if (latestBalance < 10_000.00) return@mapNotNull null

        // Daily Interest = End-of-Day Balance * (0.05 / 365), projected for 30 days
        val dailyInterest = latestBalance * (0.05 / 365.0)
        val expectedInterest = dailyInterest * 30.0
        Report2Row(name, latestBalance, expectedInterest)
    }.sortedByDescending { it.expectedInterest }

    val r2File = File("report2_interest_expense_projection.csv")
    r2File.printWriter().use { out ->
        out.println("AccountName,CurrentBalance,ExpectedInterest")
        report2Rows.forEach {
            out.println("${escapeCsvField(it.name)},\"${formatMoney(it.currentBalance)}\",\"${formatMoney(it.expectedInterest)}\"")
        }
    }

    // ---------------------------------------------------------

    data class Report3Row(
        val name: String,
        val date: LocalDate,
        val totalWithdrawals: Double,
        val numDeposits: Int,
        val totalDeposits: Double,
        val turnoverVelocity: Double,
        val riskFlag: String
    )

    val report3Rows = sortedTx.groupBy { Pair(it.accountName, it.date) }
        .map { (key, dayTxs) ->
            val (name, date) = key
            val withdrawals = dayTxs.filter { it.type == "withdrawal" }
            val deposits = dayTxs.filter { it.type == "deposit" }

            val totalW = withdrawals.sumOf { it.amount }
            val numD = deposits.size
            val totalD = deposits.sumOf { it.amount }

            val velocity = if (totalD > 0.0) (totalW / totalD) * 100.0 else if (totalW > 0.0) 100.0 else 0.0
            val flag = if (totalW >= 500_000.00 || velocity >= 90.0) "Suspicious" else "Normal"

            Report3Row(name, date, totalW, numD, totalD, velocity, flag)
        }
        // Ascending by Date, Descending by RiskFlagLevel ("Suspicious" > "Normal" alphabetically)
        .sortedWith(compareBy<Report3Row> { it.date }.thenByDescending { it.riskFlag })

    val r3File = File("report3_suspicious_client_behavior.csv")
    r3File.printWriter().use { out ->
        out.println("AccountName,Date,TotalWithdrawalAmounts,NumberOfDeposits,TotalDepositAmounts,TurnoverVelocity (%),RiskFlagLevel")
        report3Rows.forEach {
            out.println("${escapeCsvField(it.name)},${it.date.format(DATE_FORMAT)},\"${formatMoney(it.totalWithdrawals)}\",${it.numDeposits},\"${formatMoney(it.totalDeposits)}\",\"${formatMoney(it.turnoverVelocity)}\",${it.riskFlag}")
        }
    }

    // ---------------------------------------------------------

    val r4File = File("report4_custom_account_summary.csv")
    r4File.printWriter().use { out ->
        out.println("AccountName,TotalTransactions,TotalDeposits,TotalWithdrawals,NetCashFlow")
        byAccount.forEach { (name, txs) ->
            val dep = txs.filter { it.type == "deposit" }.sumOf { it.amount }
            val wit = txs.filter { it.type == "withdrawal" }.sumOf { it.amount }
            val net = dep - wit
            out.println("${escapeCsvField(name)},${txs.size},\"${formatMoney(dep)}\",\"${formatMoney(wit)}\",\"${formatMoney(net)}\"")
        }
    }

    // ---------------------------------------------------------

    val totalAccounts = byAccount.size
    val totalDeposits = sortedTx.filter { it.type == "deposit" }.sumOf { it.amount }
    val totalWithdrawals = sortedTx.filter { it.type == "withdrawal" }.sumOf { it.amount }
    val totalBalance = byAccount.values.sumOf { it.last().currentBalance }
    val totalInterestExpense = report2Rows.sumOf { it.expectedInterest }

    val jsonContent = """
        {
          "total_accounts": $totalAccounts,
          "total_deposits": ${"%.2f".format(totalDeposits)},
          "total_withdrawals": ${"%.2f".format(totalWithdrawals)},
          "total_balance": ${"%.2f".format(totalBalance)},
          "total_interest_expense": ${"%.2f".format(totalInterestExpense)}
        }
    """.trimIndent()
    File("summary.json").writeText(jsonContent)

    // ---------------------------------------------------------

    println("Outputs saved to individual files...\n")

    println("Report 1: Banking Privilege Tiers")
    println("%-22s | %-10s | %-16s | %-18s | %-10s".format("AccountName", "AccountAge", "CurrentBalance", "AverageDailyBalance", "PrivilegeTier"))
    report1Rows.take(2).forEach {
        println("%-22s | %-10d | %-16s | %-18s | %-10s".format(it.name, it.ageDays, formatMoney(it.currentBalance), formatMoney(it.adb), it.tier))
    }
    println("(Full table exported to report1_banking_privilege_tiers.csv)\n")

    println("Report 2: Interest Expense Projection")
    println("%-22s | %-16s | %-16s".format("AccountName", "CurrentBalance", "ExpectedInterest"))
    report2Rows.take(2).forEach {
        println("%-22s | %-16s | %-16s".format(it.name, formatMoney(it.currentBalance), formatMoney(it.expectedInterest)))
    }
    println("(Full table exported to report2_interest_expense_projection.csv)\n")

    println("Report 3: Suspicious Client Behavior")
    println("%-20s | %-10s | %-14s | %-8s | %-14s | %-10s | %-10s".format("AccountName", "Date", "Withdrawals", "Deposits", "DepositAmt", "Velocity%", "RiskFlag"))
    report3Rows.take(2).forEach {
        println("%-20s | %-10s | %-14s | %-8d | %-14s | %-10s | %-10s".format(
            it.name, it.date.format(DATE_FORMAT), formatMoney(it.totalWithdrawals), it.numDeposits, formatMoney(it.totalDeposits), formatMoney(it.turnoverVelocity), it.riskFlag
        ))
    }
    println("(Full table exported to report3_suspicious_client_behavior.csv)\n")

    println("Summary State (summary.json):")
    println(jsonContent)
}