// MCO1 Milestone 1 - Group 11 - CSADPRG S40D
// Eduardo Bolaños, Nicole Gendernalik, Jared Mallari, Shaun Roca

fun main() {
    var choice = 1
    while (choice != 0){
        mainMenu()
        choice =  readln().toInt()
        println("\n***")
        println("Choice = $choice")
        when(choice){
            1 -> registerMenu()
            2 -> depositMenu()
            3 -> withdrawMenu()
            4 -> currencyExchangeMenu()
            5 -> recordExchangeRatesMenu()
        }

    }
}
fun mainMenu(){
    println("\nSelect Transaction:")
    println("[1] Register Account Name")
    println("[2] Deposit Amount")
    println("[3] Withdraw Amount")
    println("[4] Currency Exchange")
    println("[5] Record Exchange Rates")
    println("[6] Show Interest Amount")
    print("\nChoice: ")
}
fun registerMenu(){
    println("\nRegister Account Name")
    print("Account Name: ")
    val name = readln()
    println("\n***")
    println("Account Name = $name")
}

fun depositMenu(){
    println("\nDeposit Amount")
    print("Account Name: ")
    val name = readln()
    println("Current Balance: 1000")
    println("Currency: PHP")
    print("\nDeposit Amount: ")
    val amount = readln()

    println("\n***")
    println("Account Name: $name")
    println("Deposit Amount: $amount")
}

fun withdrawMenu(){
    println("\nWithdraw Amount")
    print("Account Name: ")
    val name = readln()
    println("Current Balance: 1000")
    println("Currency: PHP")

    print("\nWithdraw Amount: ")
    val amount = readln().toFloat()

    println("\n***")
    println("Account Name = $name ")
    println("Withdraw Amount = $amount")
}

fun currencyExchangeMenu(){
    println("\nForeign Currency Exchange")
    print("Source Amount = ")
    val sourceAmount = readln().toFloat()

    println("\nExchanged Currency")
    println("[1] Philippine Peso (PHP) = $sourceAmount")
    println("[2] United States Dollar (USD) = " + sourceAmount * 62.00)
    println("[3] Japanese Yen (JPY) = " + sourceAmount * 0.40)
    println("[4] British Pound Sterling (GBP) = " + sourceAmount * 84.00)
    println("[5] Euro (EUR) = " + sourceAmount * 72.00)
    println("[6] Chinese Yuan Renminni(CNY) = " + sourceAmount * 9.00)

    println("\n***")
    println("Target Currency: = Philippine Peso (PHP)")
    println("Source Amount (PHP) = $sourceAmount")
}

fun recordExchangeRatesMenu(){
    println("\nRecord Exchange Rate\n")
    println("[1] Philippine Peso (PHP)")
    println("[2] US Dollar (USD)")
    println("[3] Japanese Yen (JPY)")
    println("[4] British Pound Sterling (GBP)")
    println("[5] Euro (EUR)")
    println("[6] Chinese Yuan Renminni(CNY)\n")

    print("Select foreign currency: ")
    val choice = readln()
    print("Exchange rate: ")
    val rate = readln().toFloat()

    println("\n***")
    println("Select Foreign Currency: $choice")
    println("Exchange Rate: $rate")
}
