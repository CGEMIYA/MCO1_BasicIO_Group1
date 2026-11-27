#********************************
# Last names: Roca, Gendernalik, Mallari, Bolanos
# Language: R
# Paradigm(s): Imperative
#********************************

# MCO2 - Banking Deposit and Withdrawal Reports
# Remember to run with: Rscript MP_11_R.r
#
# NOTE TO FUTURE SELF:
# Keep bank_transactions.csv in the same folder as this R file.
# The program preserves the MCO1 menu and transaction functions, while
# extending account handling and adding CSV ingestion and report generation. which is how it should work I think
#
# Data note:
# The supplied CSV contains 9,189 rows and uses these columns:
# account_name, account_number, date, time, currency, amount, type,
# current_balance.
# The assignment's report specification refers to 10,000 rows, but this
# program reports the actual number read from the supplied file. This if fine
#

#--------------------------------
# GLOBAL SETTINGS
#--------------------------------

TRANSACTION_FILE <- "bank_transactions.csv"
REPORT_FOLDER <- "."

CURRENCIES <- c("PHP", "USD", "JPY", "GBP", "EUR", "CNY")
CURRENCY_NAMES <- c(
  "Philippine Peso",
  "United States Dollar",
  "Japanese Yen",
  "British Pound Sterling",
  "Euro",
  "Chinese Yuan Renminbi"
)

ANNUAL_INTEREST_RATE <- 0.05

#--------------------------------
# GENERAL HELPER FUNCTIONS
#--------------------------------

#Use this whenever the user is expected to enter an amount. It keeps bad input from being treated like a real number.
read_number <- function(prompt_text) {
  repeat {
    value <- suppressWarnings(as.numeric(readline(prompt = prompt_text)))

    if (length(value) == 1 && !is.na(value) && is.finite(value)) {
      return(value)
    }

    cat("Invalid amount. Please enter a valid number.\n")
  }
}

# Menu input is handled separately so I do not have to repeat the same conversion every time.
read_menu_choice <- function(prompt_text) {
  value <- suppressWarnings(as.integer(readline(prompt = prompt_text)))

  if (length(value) == 0 || is.na(value)) {
    return(-1)
  }

  return(value)
}

# Small formatting helper so balances and amounts are easier to read.
format_money <- function(value) {
  return(formatC(value, format = "f", digits = 2, big.mark = ","))
}

#--------------------------------
# TRANSACTION FILE FUNCTIONS
#--------------------------------

# Return an empty table with the same columns as the CSV. This makes the other functions easier to work with.
empty_transactions <- function() {
  transactions <- data.frame(
    account_name = character(),
    account_number = numeric(),
    date = character(),
    time = character(),
    currency = character(),
    amount = numeric(),
    type = character(),
    current_balance = numeric(),
    stringsAsFactors = FALSE
  )

  return(transactions)
}

# Read the saved transactions first. I also check the column names because the rest of the program depends on them.
load_transactions <- function(file_name = TRANSACTION_FILE) {
  if (!file.exists(file_name)) {
    return(empty_transactions())
  }

  transactions <- tryCatch(
    read.csv(
      file_name,
      stringsAsFactors = FALSE,
      check.names = FALSE
    ),
    error = function(e) {
      cat(sprintf("Unable to read transaction file: %s\n", e$message))
      return(empty_transactions())
    }
  )

  required_columns <- c(
    "account_name", "account_number", "date", "time",
    "currency", "amount", "type", "current_balance"
  )

  missing_columns <- setdiff(required_columns, names(transactions))

  if (length(missing_columns) > 0) {
    cat("Transaction file is missing required columns:\n")
    print(missing_columns)
    return(empty_transactions())
  }

  transactions <- transactions[, required_columns]

  transactions$account_name <- as.character(transactions$account_name)
  transactions$account_number <- suppressWarnings(
    as.numeric(transactions$account_number)
  )
  transactions$date <- as.character(transactions$date)
  transactions$time <- as.character(transactions$time)
  transactions$currency <- toupper(as.character(transactions$currency))
  transactions$amount <- suppressWarnings(as.numeric(transactions$amount))
  transactions$type <- tolower(as.character(transactions$type))
  transactions$current_balance <- suppressWarnings(
    as.numeric(transactions$current_balance)
  )

  return(transactions)
}

# Save the full transaction table back to the CSV after it has been updated.
save_transactions <- function(transactions,
                              file_name = TRANSACTION_FILE) {
  write.csv(
    transactions,
    file = file_name,
    row.names = FALSE,
    na = ""
  )
}

# Rather than changing the CSV in several places, new deposits and withdrawals go through this function.
append_transaction <- function(acc_name, acc_number, amount,
                               transaction_type, current_balance,
                               currency = "PHP") {
  transactions <- load_transactions()

  new_transaction <- data.frame(
    account_name = acc_name,
    account_number = as.numeric(acc_number),
    date = format(Sys.Date(), "%d/%m/%Y"),
    time = format(Sys.time(), "%H:%M:%S"),
    currency = currency,
    amount = as.numeric(amount),
    type = tolower(transaction_type),
    current_balance = as.numeric(current_balance),
    stringsAsFactors = FALSE
  )

  transactions <- rbind(transactions, new_transaction)
  save_transactions(transactions)

  return(transactions)
}

#--------------------------------
# ACCOUNT MANAGEMENT
#--------------------------------

# Rebuild the account list from the transaction history when the program starts. The latest row holds the current balance.
initialize_accounts <- function() {
  accounts <- data.frame(
    AccountName = character(),
    AccountNumber = numeric(),
    CurrentBalance = numeric(),
    Currency = character(),
    OpeningDate = character(),
    stringsAsFactors = FALSE
  )

  transactions <- load_transactions()

  if (nrow(transactions) == 0) {
    return(accounts)
  }

  valid_rows <- !is.na(transactions$account_number) &
    !is.na(transactions$current_balance) &
    !is.na(transactions$date)

  transactions <- transactions[valid_rows, ]

  if (nrow(transactions) == 0) {
    return(accounts)
  }

  transactions$ParsedDate <- as.Date(
    transactions$date,
    format = "%d/%m/%Y"
  )

  transactions$ParsedTime <- suppressWarnings(
    strptime(transactions$time, format = "%H:%M:%S")
  )

  account_numbers <- unique(transactions$account_number)

  for (account_number in account_numbers) {
    account_rows <- transactions[
      transactions$account_number == account_number,
    ]

    account_rows <- account_rows[order(
      account_rows$ParsedDate,
      account_rows$time
    ), ]

    latest_row <- account_rows[nrow(account_rows), ]
    opening_date <- min(account_rows$ParsedDate, na.rm = TRUE)

    accounts <- rbind(
      accounts,
      data.frame(
        AccountName = as.character(latest_row$account_name),
        AccountNumber = as.numeric(account_number),
        CurrentBalance = as.numeric(latest_row$current_balance),
        Currency = as.character(latest_row$currency),
        OpeningDate = format(opening_date, "%d/%m/%Y"),
        stringsAsFactors = FALSE
      )
    )
  }

  rownames(accounts) <- NULL
  return(accounts)
}

# Use the highest account number already in the file, then add one so the number does not get reused.
get_next_account_number <- function() {
  transactions <- load_transactions()

  if (nrow(transactions) == 0 ||
      all(is.na(transactions$account_number))) {
    return(1092000001)
  }

  highest_number <- max(transactions$account_number, na.rm = TRUE)
  return(highest_number + 1)
}

# Show the available accounts and make sure the selected row is within the list.
select_account <- function(accounts) {
  if (nrow(accounts) == 0) {
    cat("No registered accounts are available.\n")
    return(0)
  }

  cat("\nRegistered Accounts\n")

  for (i in seq_len(nrow(accounts))) {
    cat(sprintf(
      "[%d] %s - %.0f - Balance: %s %s\n",
      i,
      accounts$AccountName[i],
      accounts$AccountNumber[i],
      accounts$Currency[i],
      format_money(accounts$CurrentBalance[i])
    ))
  }

  choice <- read_menu_choice("Select Account: ")

  if (choice < 1 || choice > nrow(accounts)) {
    cat("Invalid account selection.\n")
    return(0)
  }

  return(choice)
}

#--------------------------------
# MCO1 MENU
#--------------------------------

# Keep the main choices in one place. This is also where the user returns after finishing an action.
show_main_menu <- function() {
  cat("Select Transaction:\n")
  cat("[1] Register Account\n")
  cat("[2] Deposit Amount\n")
  cat("[3] Withdraw Amount\n")
  cat("[4] Currency Exchange\n")
  cat("[5] Record Exchange Rates\n")
  cat("[6] Show Interest Amount\n")
  cat("[7] Generate Reports\n")
  cat("[8] Exit\n\n")

  choice <- read_menu_choice("Choice: ")

  cat("\n***\n")
  cat(sprintf("Choice = %d\n\n", choice))

  return(choice)
}

#--------------------------------
# MCO1 / MCO2 ACCOUNT TRANSACTIONS
#--------------------------------

# A new account needs a name and at least PHP 5,000 to start. The first deposit is saved as a transaction too.
reg_acc <- function(accounts, next_account_number) {
  cat("Register Account\n")

  acc_name <- trimws(readline(prompt = "Account Name: "))

  while (acc_name == "") {
    cat("Account name cannot be empty.\n")
    acc_name <- trimws(readline(prompt = "Account Name: "))
  }

  initial_deposit <- read_number("Initial Deposit: ")

  while (initial_deposit < 5000) {
    cat("Initial deposit must be at least Php 5,000.00.\n")
    initial_deposit <- read_number("Initial Deposit: ")
  }

  account_number <- next_account_number
  opening_date <- format(Sys.Date(), "%d/%m/%Y")

  new_account <- data.frame(
    AccountName = acc_name,
    AccountNumber = as.numeric(account_number),
    CurrentBalance = initial_deposit,
    Currency = "PHP",
    OpeningDate = opening_date,
    stringsAsFactors = FALSE
  )

  accounts <- rbind(accounts, new_account)

  append_transaction(
    acc_name,
    account_number,
    initial_deposit,
    "deposit",
    initial_deposit,
    "PHP"
  )

  cat("\n***\n")
  cat(sprintf("Account Name = %s\n", acc_name))
  cat(sprintf("Account Number = %.0f\n", account_number))
  cat(sprintf("Initial Deposit = %.2f\n", initial_deposit))
  cat("Account created successfully!\n\n")

  return(list(
    accounts = accounts,
    next_account_number = account_number + 1
  ))
}

# Update only the selected account, then save the deposit so it is still there after restarting the program.
deposit_amount <- function(accounts, currency) {
  cat("Deposit Amount\n")

  account_index <- select_account(accounts)

  if (account_index == 0) {
    return(accounts)
  }

  acc_name <- accounts$AccountName[account_index]
  acc_number <- accounts$AccountNumber[account_index]
  cur_bal <- accounts$CurrentBalance[account_index]

  cat(sprintf("Account Name: %s\n", acc_name))
  cat(sprintf("Account Number: %.0f\n", acc_number))
  cat(sprintf("Current Balance: %.2f\n", cur_bal))
  cat(sprintf("Currency: %s\n\n", accounts$Currency[account_index]))

  deposit <- read_number("Deposit Amount: ")

  while (deposit <= 0) {
    cat("Deposit amount must be greater than zero.\n")
    deposit <- read_number("Deposit Amount: ")
  }

  cur_bal <- cur_bal + deposit
  accounts$CurrentBalance[account_index] <- cur_bal

  append_transaction(
    acc_name, acc_number, deposit, "deposit", cur_bal,
    accounts$Currency[account_index]
  )

  cat("\n***\n")
  cat(sprintf("Account Name = %s\n", acc_name))
  cat(sprintf("Deposit Amount = %.2f\n", deposit))
  cat(sprintf("Updated Balance = %.2f\n\n", cur_bal))

  return(accounts)
}

# Do not allow zero, negative, or over-balance withdrawals. The updated balance is recorded in the same way as a deposit.
withdraw_amount <- function(accounts, currency) {
  cat("Withdraw Amount\n")

  account_index <- select_account(accounts)

  if (account_index == 0) {
    return(accounts)
  }

  acc_name <- accounts$AccountName[account_index]
  acc_number <- accounts$AccountNumber[account_index]
  cur_bal <- accounts$CurrentBalance[account_index]

  cat(sprintf("Account Name: %s\n", acc_name))
  cat(sprintf("Account Number: %.0f\n", acc_number))
  cat(sprintf("Current Balance: %.2f\n", cur_bal))
  cat(sprintf("Currency: %s\n\n", accounts$Currency[account_index]))

  withdraw <- read_number("Withdraw Amount: ")

  while (withdraw <= 0 || withdraw > cur_bal) {
    cat("Invalid withdrawal. Enter a positive amount not exceeding the balance.\n")
    withdraw <- read_number("Withdraw Amount: ")
  }

  cur_bal <- cur_bal - withdraw
  accounts$CurrentBalance[account_index] <- cur_bal

  append_transaction(
    acc_name, acc_number, withdraw, "withdrawal", cur_bal,
    accounts$Currency[account_index]
  )

  cat("\n***\n")
  cat(sprintf("Account Name = %s\n", acc_name))
  cat(sprintf("Withdraw Amount = %.2f\n", withdraw))
  cat(sprintf("Updated Balance = %.2f\n\n", cur_bal))

  return(accounts)
}

#--------------------------------
# MCO1 CURRENCY EXCHANGE
#--------------------------------

# Let the user update one foreign currency rate at a time. Rates are stored as PHP for one unit of foreign currency.
record_ex_rate <- function(ex_rate) {
  cat("Record Exchange Rate\n\n")

  for (i in seq_along(CURRENCIES)) {
    cat(sprintf("[%d] %s (%s)\n", i, CURRENCY_NAMES[i], CURRENCIES[i]))
  }

  ex_choice <- read_menu_choice("Select Foreign Currency: ")

  if (ex_choice < 1 || ex_choice > length(ex_rate)) {
    cat("Invalid currency selection.\n")
    return(ex_rate)
  }

  rate_input <- read_number("Exchange Rate (PHP per 1 foreign currency): ")

  while (rate_input <= 0) {
    cat("Exchange rate must be greater than zero.\n")
    rate_input <- read_number("Exchange Rate: ")
  }

  ex_rate[ex_choice] <- rate_input

  cat("\n***\n")
  cat(sprintf("Select Foreign Currency = [%d]\n", ex_choice))
  cat(sprintf("Exchange Rate = %.2f\n\n", rate_input))

  return(ex_rate)
}

# Convert the source amount to PHP first, then use that PHP value to show the other currencies.
foreign_currency_exchange <- function(ex_rate) {
  cat("Foreign Currency Exchange\n")
  cat("Source Currency Option:\n")

  for (i in seq_along(CURRENCIES)) {
    cat(sprintf("[%d] %s (%s)\n", i, CURRENCY_NAMES[i], CURRENCIES[i]))
  }

  source_opt <- read_menu_choice("Source Currency: ")

  if (source_opt < 1 || source_opt > length(ex_rate)) {
    cat("Invalid source currency.\n")
    return(invisible(NULL))
  }

  source_amount <- read_number("Source Amount: ")

  while (source_amount < 0) {
    cat("Amount cannot be negative.\n")
    source_amount <- read_number("Source Amount: ")
  }

  # Exchange rates are represented as PHP per one unit of currency.
  php_amount <- source_amount * ex_rate[source_opt]

  cat("\nExchanged Currency Options:\n")

  for (i in seq_along(CURRENCIES)) {
    converted_amount <- php_amount / ex_rate[i]
    cat(sprintf(
      "[%d] %s (%s) = %.2f\n",
      i, CURRENCY_NAMES[i], CURRENCIES[i], converted_amount
    ))
  }

  cat("\n***\n")
  cat(sprintf("Source Amount = %.2f %s\n\n",
              source_amount, CURRENCIES[source_opt]))
}

# Display the estimated daily interest without changing the actual account balance.
show_interest_amount <- function(accounts) {
  cat("Show Interest Amount\n")

  account_index <- select_account(accounts)

  if (account_index == 0) {
    return(invisible(NULL))
  }

  acc_name <- accounts$AccountName[account_index]
  cur_bal <- accounts$CurrentBalance[account_index]
  currency <- accounts$Currency[account_index]

  cat(sprintf("Account Name: %s\n", acc_name))
  cat(sprintf("Current Balance: %.2f\n", cur_bal))
  cat(sprintf("Currency: %s\n", currency))
  cat("Interest Rate: 5%\n")

  days <- read_menu_choice("Total Number of Days: ")

  while (days < 0) {
    cat("Number of days cannot be negative.\n")
    days <- read_menu_choice("Total Number of Days: ")
  }

  cat(sprintf("Total Number of Days: %d days\n\n", days))
  cat("Day\tInterest\tBalance\n")

  temp_balance <- cur_bal

  if (days > 0) {
    for (day in seq_len(days)) {
      interest <- temp_balance * (ANNUAL_INTEREST_RATE / 365)
      temp_balance <- temp_balance + interest
      cat(sprintf("%d\t%.2f\t\t%.2f\n",
                  day, interest, temp_balance))
    }
  }

  cat("\n***\n")
  return(invisible(temp_balance))
}

#--------------------------------
# DATA INGESTION AND VALIDATION
#--------------------------------

# Prepare the CSV for the reports. I check the expected columns and convert values into usable types here.
load_and_clean_data <- function(file_name = TRANSACTION_FILE) {
  cat("Processing dataset...\n")

  if (!file.exists(file_name)) {
    cat(sprintf("File not found: %s\n", file_name))
    return(NULL)
  }

  raw_data <- tryCatch(
    read.csv(file_name, stringsAsFactors = FALSE),
    error = function(e) {
      cat(sprintf("CSV read error: %s\n", e$message))
      return(NULL)
    }
  )

  if (is.null(raw_data)) {
    return(NULL)
  }

  total_rows <- nrow(raw_data)
  cat(sprintf("Total rows read: %d\n", total_rows))

  required_columns <- c(
    "account_name", "account_number", "date", "time",
    "currency", "amount", "type", "current_balance"
  )

  missing_columns <- setdiff(required_columns, names(raw_data))

  if (length(missing_columns) > 0) {
    cat("Missing required columns:\n")
    print(missing_columns)
    return(NULL)
  }

  data <- raw_data[, required_columns]

  data$account_name <- trimws(as.character(data$account_name))
  data$account_number <- suppressWarnings(as.numeric(data$account_number))
  data$date_parsed <- as.Date(data$date, format = "%d/%m/%Y")
  data$time <- as.character(data$time)
  data$currency <- toupper(trimws(as.character(data$currency)))
  data$amount <- suppressWarnings(as.numeric(data$amount))
  data$type <- tolower(trimws(as.character(data$type)))
  data$current_balance <- suppressWarnings(as.numeric(data$current_balance))

  missing_rows <- !complete.cases(data[, required_columns])
  invalid_date <- is.na(data$date_parsed)
  invalid_amount <- is.na(data$amount) | data$amount < 0
  invalid_balance <- is.na(data$current_balance)
  invalid_type <- !(data$type %in% c("deposit", "withdrawal"))
  invalid_account <- is.na(data$account_number)

  error_rows <- missing_rows | invalid_date | invalid_amount |
    invalid_balance | invalid_type | invalid_account

  cat(sprintf("Rows with missing or invalid data: %d\n", sum(error_rows)))

  data <- data[!error_rows, ]

  php_data <- data[data$currency == "PHP", ]

  cat(sprintf("Rows remaining after validation: %d\n", nrow(data)))
  cat(sprintf("PHP transaction rows: %d\n", nrow(php_data)))

  php_data <- php_data[order(
    php_data$account_number,
    php_data$date_parsed,
    php_data$time
  ), ]

  rownames(php_data) <- NULL
  return(php_data)
}

#--------------------------------
# REPORT HELPER FUNCTIONS
#--------------------------------

# Reports should use each account latest recorded balance, not add every historical balance together.
get_latest_account_balances <- function(data) {
  if (is.null(data) || nrow(data) == 0) {
    return(data.frame())
  }

  data <- data[order(
    data$account_number,
    data$date_parsed,
    data$time
  ), ]

  latest_rows <- data[
    !duplicated(data$account_number, fromLast = TRUE),
  ]

  return(latest_rows)
}

# Work out the end-of-day balance for each date. This is used when calculating average daily balance.
get_daily_account_balances <- function(account_data, month_start,
                                       month_end) {
  account_data <- account_data[
    order(account_data$date_parsed, account_data$time),
  ]

  opening_rows <- account_data[
    account_data$date_parsed < month_start,
  ]

  if (nrow(opening_rows) > 0) {
    starting_balance <- opening_rows$current_balance[nrow(opening_rows)]
  } else {
    first_month_row <- account_data[
      account_data$date_parsed >= month_start &
        account_data$date_parsed <= month_end,
    ]

    if (nrow(first_month_row) == 0) {
      return(NULL)
    }

    first_type <- first_month_row$type[1]
    first_amount <- first_month_row$amount[1]
    first_balance <- first_month_row$current_balance[1]

    if (first_type == "deposit") {
      starting_balance <- first_balance - first_amount
    } else {
      starting_balance <- first_balance + first_amount
    }
  }

  month_dates <- seq(month_start, month_end, by = "day")
  daily_balances <- numeric(length(month_dates))
  running_balance <- starting_balance

  for (i in seq_along(month_dates)) {
    day_rows <- account_data[
      account_data$date_parsed == month_dates[i],
    ]

    if (nrow(day_rows) > 0) {
      day_rows <- day_rows[order(day_rows$time), ]
      running_balance <- day_rows$current_balance[nrow(day_rows)]
    }

    daily_balances[i] <- running_balance
  }

  return(data.frame(
    Date = month_dates,
    EndOfDayBalance = daily_balances
  ))
}

#--------------------------------
# REPORT 1: BANKING PRIVILEGE TIERS
#--------------------------------

# Report 1 groups accounts into privilege tiers using their average daily balance.
generate_report1 <- function(data) {
  cat("\nReport 1: Banking Privilege Tiers\n")

  if (is.null(data) || nrow(data) == 0) {
    cat("No PHP transaction data available.\n")
    return(data.frame())
  }

  current_month_start <- as.Date(format(Sys.Date(), "%Y-%m-01"))
  last_month_start <- seq(
    current_month_start,
    by = "-1 month",
    length.out = 2
  )[2]
  last_month_end <- current_month_start - 1

  account_numbers <- unique(data$account_number)
  report <- data.frame()

  for (account_number in account_numbers) {
    account_data <- data[data$account_number == account_number, ]
    daily_data <- get_daily_account_balances(
      account_data, last_month_start, last_month_end
    )

    if (is.null(daily_data)) {
      next
    }

    average_daily_balance <- mean(daily_data$EndOfDayBalance)
    current_row <- account_data[nrow(account_data), ]
    opening_date <- min(account_data$date_parsed, na.rm = TRUE)
    account_age <- as.integer(Sys.Date() - opening_date)

    if (average_daily_balance < 100000) {
      next
    }

    if (average_daily_balance < 500000) {
      privilege_tier <- "Silver"
    } else if (average_daily_balance < 1000000) {
      privilege_tier <- "Gold"
    } else {
      privilege_tier <- "Platinum"
    }

    report <- rbind(
      report,
      data.frame(
        AccountName = current_row$account_name,
        AccountAge = account_age,
        CurrentBalance = current_row$current_balance,
        AverageDailyBalance = average_daily_balance,
        PrivilegeTier = privilege_tier,
        stringsAsFactors = FALSE
      )
    )
  }

  if (nrow(report) > 0) {
    report <- report[order(-report$AverageDailyBalance), ]
  }

  rownames(report) <- NULL

  write.csv(
    report,
    "report1_banking_privilege_tiers.csv",
    row.names = FALSE,
    na = ""
  )

  print(utils::head(report, 2))
  cat("Full table exported to report1_banking_privilege_tiers.csv\n")

  return(report)
}

#--------------------------------
# REPORT 2: INTEREST EXPENSE PROJECTION
#--------------------------------

# This is a projection based on the current balance and the daily interest rate for 30 days.
generate_report2 <- function(data) {
  cat("\nReport 2: Interest Expense Projection\n")

  if (is.null(data) || nrow(data) == 0) {
    cat("No PHP transaction data available.\n")
    return(data.frame())
  }

  latest_data <- get_latest_account_balances(data)
  latest_data <- latest_data[
    latest_data$current_balance >= 10000,
  ]

  report <- data.frame()

  if (nrow(latest_data) > 0) {
    expected_interest <- latest_data$current_balance *
      (ANNUAL_INTEREST_RATE / 365) * 30

    report <- data.frame(
      AccountName = latest_data$account_name,
      CurrentBalance = latest_data$current_balance,
      ExpectedInterest = expected_interest,
      stringsAsFactors = FALSE
    )

    report <- report[order(-report$ExpectedInterest), ]
  }

  rownames(report) <- NULL

  write.csv(
    report,
    "report2_interest_expense_projection.csv",
    row.names = FALSE,
    na = ""
  )

  print(utils::head(report, 2))
  cat("Full table exported to report2_interest_expense_projection.csv\n")

  return(report)
}

#--------------------------------
# REPORT 3: SUSPICIOUS CLIENT BEHAVIOR
#--------------------------------

# Check daily deposits and withdrawals for each account. The thresholds below determine whether the activity is flagged.
generate_report3 <- function(data) {
  cat("\nReport 3: Suspicious Client Behavior\n")

  if (is.null(data) || nrow(data) == 0) {
    cat("No PHP transaction data available.\n")
    return(data.frame())
  }

  grouped_keys <- unique(data[, c(
    "account_number", "account_name", "date", "date_parsed"
  )])

  report <- data.frame()

  for (i in seq_len(nrow(grouped_keys))) {
    account_number <- grouped_keys$account_number[i]
    account_name <- grouped_keys$account_name[i]
    transaction_date <- grouped_keys$date[i]
    parsed_date <- grouped_keys$date_parsed[i]

    daily_data <- data[
      data$account_number == account_number &
        data$date_parsed == parsed_date,
    ]

    withdrawals <- daily_data[
      daily_data$type == "withdrawal", "amount"
    ]

    deposits <- daily_data[
      daily_data$type == "deposit", "amount"
    ]

    total_withdrawals <- sum(withdrawals)
    number_of_deposits <- length(deposits)
    total_deposits <- sum(deposits)

    if (total_deposits == 0) {
      if (total_withdrawals > 0) {
        turnover_velocity <- Inf
      } else {
        turnover_velocity <- 0
      }
    } else {
      turnover_velocity <- total_withdrawals /
        total_deposits * 100
    }

    if (total_withdrawals >= 500000 ||
        turnover_velocity >= 90) {
      risk_flag <- "Suspicious"
    } else {
      risk_flag <- "Normal"
    }

    report <- rbind(
      report,
      data.frame(
        AccountName = account_name,
        Date = transaction_date,
        TotalWithdrawalAmounts = total_withdrawals,
        NumberOfDeposits = number_of_deposits,
        TotalDepositAmounts = total_deposits,
        TurnoverVelocity = turnover_velocity,
        RiskFlagLevel = risk_flag,
        stringsAsFactors = FALSE
      )
    )
  }

  report$SortDate <- as.Date(report$Date, format = "%d/%m/%Y")
  report$RiskOrder <- ifelse(
    report$RiskFlagLevel == "Suspicious", 1, 0
  )

  report <- report[order(
    report$SortDate,
    -report$RiskOrder
  ), ]

  report$SortDate <- NULL
  report$RiskOrder <- NULL
  rownames(report) <- NULL

  write.csv(
    report,
    "report3_suspicious_client_behavior.csv",
    row.names = FALSE,
    na = ""
  )

  print(utils::head(report, 2))
  cat("Full table exported to report3_suspicious_client_behavior.csv\n")

  return(report)
}

#--------------------------------
# REPORT 4: CUSTOMIZED MONTHLY ACCOUNT ACTIVITY
#--------------------------------

# This is the additional monthly view: deposits, withdrawals, and the net cash flow for each account.
generate_report4 <- function(data) {
  cat("\nReport 4: Monthly Account Activity\n")

  if (is.null(data) || nrow(data) == 0) {
    cat("No PHP transaction data available.\n")
    return(data.frame())
  }

  data$Month <- format(data$date_parsed, "%Y-%m")

  grouped <- unique(data[, c(
    "account_number", "account_name", "Month"
  )])

  report <- data.frame()

  for (i in seq_len(nrow(grouped))) {
    account_number <- grouped$account_number[i]
    account_name <- grouped$account_name[i]
    month_value <- grouped$Month[i]

    monthly_data <- data[
      data$account_number == account_number &
        data$Month == month_value,
    ]

    total_deposits <- sum(
      monthly_data$amount[monthly_data$type == "deposit"]
    )

    total_withdrawals <- sum(
      monthly_data$amount[monthly_data$type == "withdrawal"]
    )

    report <- rbind(
      report,
      data.frame(
        AccountName = account_name,
        Month = month_value,
        TotalDeposits = total_deposits,
        TotalWithdrawals = total_withdrawals,
        NetCashFlow = total_deposits - total_withdrawals,
        stringsAsFactors = FALSE
      )
    )
  }

  report <- report[order(report$Month, report$AccountName), ]
  rownames(report) <- NULL

  write.csv(
    report,
    "report4_monthly_account_activity.csv",
    row.names = FALSE,
    na = ""
  )

  print(utils::head(report, 2))
  cat("Full table exported to report4_monthly_account_activity.csv\n")

  return(report)
}

#--------------------------------
# SUMMARY.JSON
#--------------------------------

# Keep the overall figures in one small JSON file so they can be checked without opening every report.
generate_summary_json <- function(data, report2) {
  if (is.null(data) || nrow(data) == 0) {
    total_accounts <- 0
    total_deposits <- 0
    total_withdrawals <- 0
    total_balance <- 0
  } else {
    latest_data <- get_latest_account_balances(data)

    total_accounts <- nrow(latest_data)
    total_deposits <- sum(data$amount[data$type == "deposit"])
    total_withdrawals <- sum(data$amount[data$type == "withdrawal"])
    total_balance <- sum(latest_data$current_balance)
  }

  total_interest_expense <- if (nrow(report2) == 0) {
    0
  } else {
    sum(report2$ExpectedInterest)
  }

  summary_text <- paste0(
    "{\n",
    '  "total_accounts": ', total_accounts, ",\n",
    '  "total_deposits": ', sprintf("%.2f", total_deposits), ",\n",
    '  "total_withdrawals": ', sprintf("%.2f", total_withdrawals), ",\n",
    '  "total_balance": ', sprintf("%.2f", total_balance), ",\n",
    '  "total_interest_expense": ',
    sprintf("%.2f", total_interest_expense), "\n",
    "}\n"
  )

  writeLines(summary_text, "summary.json")
  cat("\nSummary State (summary.json):\n")
  cat(summary_text)

  return(invisible(NULL))
}

#--------------------------------
# REPORT MENU
#--------------------------------

# The file has to be loaded before generating reports. Keeping that step explicit avoids running reports on missing data.
generate_reports_menu <- function() {
  data <- NULL

  repeat {
    cat("\nGenerate Reports\n\n")
    cat("[1] Load the file\n")
    cat("[2] Generate Reports\n")
    cat("[3] Back to Main Menu\n\n")

    choice <- read_menu_choice("Enter choice: ")

    switch(as.character(choice),
      "1" = {
        data <- load_and_clean_data()
      },
      "2" = {
        if (is.null(data)) {
          cat("Please load the file first.\n")
        } else {
          cat("\nGenerating reports...\n")
          cat("Outputs saved to individual files...\n")

          report1 <- generate_report1(data)
          report2 <- generate_report2(data)
          report3 <- generate_report3(data)
          report4 <- generate_report4(data)

          generate_summary_json(data, report2)
        }
      },
      "3" = {
        break
      },
      {
        cat("Invalid choice. Please select between 1 and 3.\n")
      }
    )
  }
}

#--------------------------------
# MAIN PROGRAM!!!!!!!!!!!!
#--------------------------------

# Start by rebuilding the saved accounts, then keep showing the menu until the user chooses Exit.
#rewriting mc01 menu as well
main <- function() {
  choice <- 0
  accounts <- initialize_accounts()
  next_account_number <- get_next_account_number()
  currency <- "PHP"

  # MCO1 exchange rates represented as PHP per one unit of currency.
  ex_rate <- c(1.00, 62.00, 0.40, 84.00, 72.00, 9.00)

  repeat {
    choice <- show_main_menu()

    switch(as.character(choice),
      "1" = {
        repeat {
          reg_res <- reg_acc(accounts, next_account_number)
          accounts <- reg_res$accounts
          next_account_number <- reg_res$next_account_number

          cont <- toupper(readline(
            prompt = "Register another account? (Y/N): "
          ))

          if (cont != "Y") {
            break
          }
        }
      },
      "2" = {
        accounts <- deposit_amount(accounts, currency)
      },
      "3" = {
        accounts <- withdraw_amount(accounts, currency)
      },
      "4" = {
        foreign_currency_exchange(ex_rate)
      },
      "5" = {
        ex_rate <- record_ex_rate(ex_rate)
      },
      "6" = {
        show_interest_amount(accounts)
      },
      "7" = {
        generate_reports_menu()
        accounts <- initialize_accounts()
        next_account_number <- get_next_account_number()
      },
      "8" = {
        cat("Exiting application. Thank you!\n")
        break
      },
      {
        cat("Invalid choice. Please select between 1 and 8.\n")
      }
    )

    if (choice != 8) {
      cont <- toupper(readline(
        prompt = "Back to the Main Menu (Y/N): "
      ))

      if (cont != "Y") {
        cat("Exiting application. Thank you!\n")
        break
      }

      cat("\n")
    }
  }
}

main()
