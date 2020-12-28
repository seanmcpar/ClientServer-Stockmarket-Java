package Client;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client implements AutoCloseable {
    private final int port = 10001;
    private Scanner scanner;
    private PrintWriter writer;
    private long traderId;
    private long currentHolderId;
    private long numberOfTraders;
    private ArrayList<Long> traders = new ArrayList<>();

    public Client() throws Exception {
        Socket socket = new Socket("localhost", port);
        scanner = new Scanner(socket.getInputStream());
        writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println("CONNECT");
    }

    public void RunClient()
    {
        traderId = Long.parseLong(scanner.nextLine());
        currentHolderId = Long.parseLong(scanner.nextLine());
        numberOfTraders = Integer.parseInt(scanner.nextLine());

        for (int i = 0; i < numberOfTraders; i++)
        {
            long traderId = Long.parseLong(scanner.nextLine());
            traders.add(traderId);
        }

        DisplayMarketToConsole(null);
        while (true)
        {

            try
            {
                String message = null;
                String command = scanner.nextLine();

                switch (command)
                {
                    case "NEW_TRADER":
                        long newTrader = Long.parseLong(scanner.nextLine());
                        numberOfTraders++;
                        traders.add(newTrader);
                        message = "New trader joined - Trader " + newTrader;
                        break;

                    case "NEW_STOCKHOLDER":
                        long newHolder = Long.parseLong(scanner.nextLine());
                        message = "Stock has been traded to "+ newHolder +".";
                        currentHolderId = newHolder;
                        break;

                    case "TRADER_DISCONNECTED":
                        numberOfTraders--;
                        long disconnectedTrader = Long.parseLong(scanner.nextLine());
                        message = "Trader " + disconnectedTrader+ " has left the market.";
                        traders.remove(disconnectedTrader);
                        break;

                    case "RECEIVE_TRADE":
                        message = "You have been traded the stock.";
                        ReceiveTrade();
                        break;

                    case "CONFIRM_TRADE":
                        long confirmedNewHolder = Long.parseLong(scanner.nextLine());
                        currentHolderId = confirmedNewHolder;
                        message = "You have traded the stock to "+confirmedNewHolder+".";
                        break;
                }
                DisplayMarketToConsole(message);
            }
            catch (Exception ex)
            {
                //restart server
            }
        }
    }

    public void DisplayMarketToConsole(String message)
    {
        System.out.println("___________________________________________________________________");
        if (message != null)
        {
            System.out.println("Update: " + message);
        }
        System.out.println("\nYour ID: "+ traderId);
        System.out.println("\nCurrent Stock Holder ID: "+ currentHolderId);
        System.out.println("\n" + numberOfTraders +" traders in Market:");
        for (int i = 0; i < numberOfTraders; i++)
        {
            System.out.println("Trader " + traders.get(i) + ": Has stock? " + (traders.get(i)==currentHolderId));
        }
        if (currentHolderId == traderId)
        {
            System.out.println("\nEnter T to trade stock.");
        }
    }

    public void ReceiveTrade()
    {
        currentHolderId = traderId;
        System.out.println("You have been traded the stock.");
        System.out.println("\nEnter T to trade stock.");
    }

    public void Trade(long newHolder)
    {
        if (traders.contains(newHolder))
        {
            writer.println("START_TRADE "+ newHolder);
        }
        else
        {
            System.out.println("\nUnknown Trader.");
            System.out.println("\nEnter T to trade stock.");
        }
    }

    public boolean IsCurrentStockHolder()
    {
        return currentHolderId == traderId;
    }

    @Override
    public void close() throws Exception {
        scanner.close();
        writer.close();
    }
}
