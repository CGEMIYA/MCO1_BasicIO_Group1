public class Account {
    private String AccountName;
    private float Balance;

    public Account(String NewName, float NewBalance) {
        this.AccountName = NewName;
        this.Balance = NewBalance;
    }

    public String GetAccountName() {
        return this.AccountName;
    }
    
    public float GetBalance() {
        return this.Balance;
    }

    public void SetAccountName(String NewName) {
        this.AccountName = NewName;
    }

    public void SetBalance(float NewBalance) {
        this.Balance = NewBalance;
    }
}