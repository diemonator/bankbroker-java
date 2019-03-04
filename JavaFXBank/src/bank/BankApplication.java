package bank;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class BankApplication extends Application implements ICentralBank {

    private static final int NROF_ACCOUNTS = 8;
    private static final int INIT_BALANCE = 100;

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private GridPane grid;
    private Label nrofBrokersLabel;
    private Label sumLabel;
    private Label transactionLabel;
    private Label[] accountLabel;
    private Account[] accountList;
    private final int[] balanceList = new int[NROF_ACCOUNTS];
    private int nrofTransactions = 0;
    private ThreadGroup tg;
    private int brokerId = 0;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        
        grid = new GridPane();

        // For debug purposes
        // Make de grid lines visible
        grid.setGridLinesVisible(true);

        Button btnStart = new Button();
        btnStart.setText("start");
        grid.add(btnStart, 0, 1);

        Button btnStop = new Button();
        btnStop.setText("stop");
        grid.add(btnStop, 1, 1);

        grid.add(new Label("#brokers:"), 0, 2);
        grid.add(new Label("sum:"), 0, 3);
        grid.add(new Label("#transactions: "), 0, 4);
        sumLabel = new Label("<empty>");
        nrofBrokersLabel = new Label("<empty>");
        transactionLabel = new Label("<empty>");
        grid.add(nrofBrokersLabel, 1, 2);
        grid.add(sumLabel, 1, 3);
        grid.add(transactionLabel, 1, 4);

        accountLabel = new Label[NROF_ACCOUNTS];
        accountList = new Account[NROF_ACCOUNTS];
        for (int i = 0; i < NROF_ACCOUNTS; i++) {
            balanceList[i] = INIT_BALANCE;
            accountList[i] = new Account(INIT_BALANCE);

            accountLabel[i] = new Label("<empty>");
            grid.add(new Label("account " + (1 + i) + ":"), 0, 5 + i);
            grid.add(accountLabel[i], 1, 5 + i);
        }

        btnStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                startBrokers();
            }
        });
        btnStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tg.interrupt();
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(grid);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Bank");
        primaryStage.setScene(scene);
        primaryStage.show();

        new ThreadOverview();
    }

    private void startBroker() {
        Broker b = new Broker(brokerId++, this);
        Thread t = new Thread(tg, b, "Broker-" + brokerId);
        t.start();
    }

    private void startBrokers() {
        tg = new ThreadGroup("brokers");
        CentralBankBackground cbb = new CentralBankBackground();
        Thread t = new Thread(tg, cbb, "CentralBank");
        t.start();

        startBroker();
    }

    @Override
    public int getNrofAccounts() {
        return (NROF_ACCOUNTS);
    }

    @Override
    public Account getAccount(int clientId) {
        return (accountList[clientId]);
    }

    @Override
    public void transaction(int from, int to, int balanceFrom, int balanceTo) {
        synchronized (balanceList) {
            nrofTransactions++;
            balanceList[from] = balanceFrom;
            balanceList[to] = balanceTo;
        }
    }

    class CentralBankDisplay implements Runnable {

        @Override
        public void run() {
            int sum = 0;
            synchronized (balanceList) {
                for (int i = 0; i < NROF_ACCOUNTS; i++) {
                    accountLabel[i].setText("" + balanceList[i]);
                    sum += balanceList[i];
                }
            }
            //System.out.println("sum=" + sum + " #" + nrofTransactions);
            sumLabel.setText("" + sum);
            nrofBrokersLabel.setText("" + brokerId);
            transactionLabel.setText("" + nrofTransactions);
        }

    }

    class CentralBankBackground implements Runnable {

        int i = 0;

        @Override
        public void run() {
            CentralBankDisplay cbd = new CentralBankDisplay();
            try {
                while (true) {
                    Platform.runLater(cbd); // show all accounts
                    Thread.sleep(100);
                    i++;
                    
                    // every 4s: start an extra broker
                    if ((i % 40) == 0) {
                        startBroker();
                    }
                }
            } catch (InterruptedException ex) {
                System.out.println("CB:" + ex.toString());
            }
            System.out.println("CentralBank:ready");
        }
    }
}
