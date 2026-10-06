#********************
#Last names: Roca, Gendernalik, Mallari, Bolanos
#Language: R
#Paradigm(s): Imperative
#********************


show_main_menu <- function() {
  cat("Select Transaction:\n")
  cat("[1] Register Account Name\n")
  cat("[2] Deposit Amount\n")
  cat("[3] Withdraw Amount\n")
  cat("[4] Currency Exchange\n")
  cat("[5] Record Exchange Rates\n")
  cat("[6] Show Interest Amount\n")
  
  choice <- as.integer(readline(prompt = "Choice: "))
  
  cat("\n***\n")
  cat(sprintf("Choice = %d\n\n", choice))
  
  return(choice)
}

reg_acc <- function() {
  cat("Register Account Name\n")
  acc_name <- readline(prompt = "Account Name: ")
  
  cat("\n***\n")
  cat(sprintf("Account Name = %s\n\n", acc_name))
  
  return(acc_name)
}

deposit_amount <- function(acc_name, cur_bal, currency) {
  cat("Deposit Amount\n")
  cat(sprintf("Account Name: %s\n", acc_name))
  cat(sprintf("Current Balance: %.2f\n", cur_bal))
  cat(sprintf("Currency: %s\n\n", currency))
  
  deposit <- as.numeric(readline(prompt = "Deposit Amount: "))
  cur_bal <- cur_bal + deposit
  
  cat("\n***\n")
  cat(sprintf("Account Name = %s\n", acc_name))
  cat(sprintf("Deposit Amount = %.2f\n\n", deposit))
  
  return(cur_bal)
}

withdraw_amount <- function(acc_name, cur_bal, currency) {
  cat("Withdraw Amount\n")
  cat(sprintf("Account Name: %s\n", acc_name))
  cat(sprintf("Current Balance: %.2f\n", cur_bal))
  cat(sprintf("Currency: %s\n\n", currency))
  
  withdraw <- as.numeric(readline(prompt = "Withdraw Amount: "))
  cur_bal <- cur_bal - withdraw
  
  cat("\n***\n")
  cat(sprintf("Account Name = %s\n", acc_name))
  cat(sprintf("Withdraw Amount = %.2f\n\n", withdraw))
  
  return(cur_bal)
}

record_ex_rate <- function(ex_rate) {
  cat("Record Exchange Rate\n\n")
  cat("[1] Philippine Peso (PHP)\n")
  cat("[2] United States Dollar (USD)\n")
  cat("[3] Japanese Yen (JPY)\n")
  cat("[4] British Pound Sterling (GBP)\n")
  cat("[5] Euro (EUR)\n")
  cat("[6] Chinese Yuan Renminni (CNY)\n\n")
  
  ex_choice <- as.integer(readline(prompt = "Select Foreign Currency: "))
  rate_input <- as.numeric(readline(prompt = "Exchange Rate: "))
  
  ex_rate[ex_choice] <- rate_input
  
  cat("\n***\n")
  cat(sprintf("Select Foreign Currency = [%d]\n", ex_choice))
  cat(sprintf("Exchange Rate = %.2f\n\n", rate_input))
  
  return(list(ex_choice = ex_choice, ex_rate = ex_rate))
}

foreign_currency_exchange <- function(ex_rate) {
  cat("Foreign Currency Exchange\n")
  source_amount <- as.numeric(readline(prompt = "Source Amount = "))
  
  cat("\nExchanged Currency\n")
  cat(sprintf("[1] Philippine Peso (PHP) = %.2f\n", ex_rate[1] * source_amount))
  cat(sprintf("[2] United States Dollar (USD) = %.2f\n", ex_rate[2] * source_amount))
  cat(sprintf("[3] Japanese Yen (JPY) = %.2f\n", ex_rate[3] * source_amount))
  cat(sprintf("[4] British Pound Sterling (GBP) = %.2f\n", ex_rate[4] * source_amount))
  cat(sprintf("[5] Euro (EUR) = %.2f\n", ex_rate[5] * source_amount))
  cat(sprintf("[6] Chinese Yuan Renminni (CNY) = %.2f\n", ex_rate[6] * source_amount))
  
  cat("\n***\n")
  cat("Target Currency = Philippine Peso (PHP)\n")
  cat(sprintf("Source Amount (PHP) = %.2f\n\n", source_amount))
}

main <- function() {
  choice <- 0
  acc_name <- ""
  cur_bal <- 1000.00
  currency <- "PHP"
  ex_choice <- 0
  ex_rate <- c(1.00, 62.00, 0.40, 84.00, 72.00, 9.00)
  
  choice <- show_main_menu()
  acc_name <- reg_acc()
  cur_bal <- deposit_amount(acc_name, cur_bal, currency)
  cur_bal <- withdraw_amount(acc_name, cur_bal, currency)
  
  rec_res <- record_ex_rate(ex_rate)
  ex_choice <- rec_res$ex_choice
  ex_rate <- rec_res$ex_rate
  
  foreign_currency_exchange(ex_rate)
}

main()