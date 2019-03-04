package bank;

/**
 *
 * @author Joris
 */
public class Account {

    private int balance;

    public Account(int money) {
        this.balance = money;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int money) {
        this.balance = money;
    }
}
