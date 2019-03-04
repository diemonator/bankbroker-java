package bank;

/**
 *
 * @author Joris
 */
public interface ICentralBank {
    
    int getNrofAccounts();
    Account getAccount(int clientId);
    void transaction (int from, int to, int balanceFrom, int balanceTo);
    
}
