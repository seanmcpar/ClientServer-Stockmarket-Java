package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class StockMarketServer {

    private static int port = 10001;
    private long currentHolder;
    private long nextTraderId;
    private static List<Long> traderIDs;
    private static HashMap<Long, PrintWriter> allWriters;
    private static HashMap<Long, Scanner> allReaders;
    private Object _lock = new Object();

    public StockMarketServer()
    {
        currentHolder = 0;
        nextTraderId = 0;
        allWriters = new HashMap<>();
        allReaders = new HashMap<>();
        traderIDs = new ArrayList<>();
    }

    public void RunServer()
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println("Waiting for incoming connections...");

            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    Scanner scanner = new Scanner(socket.getInputStream());
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                    long traderId = GetNextTraderId();

                    if (currentHolder == 0) {
                        currentHolder = traderId;
                        System.out.println("Stock has been given to Trader " + currentHolder + ".");
                    }

                    allReaders.put(traderId, scanner);
                    allWriters.put(traderId, writer);
                    traderIDs.add(traderId);

                    Runnable runnable = () -> {
                        ProcessIncomingRequests(scanner, traderId);
                    };
                    Thread t = new Thread(runnable);
                    t.start();
                }
            }
            catch (Exception ex){
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }



    private void ProcessIncomingRequests(Scanner scanner, long traderId)
    {
            try
            {
                String command = scanner.nextLine();

                if (command.equals("CONNECT"))
                {
                    ProcessNewConnection(traderId);
                    BroadcastNewTraderJoined(traderId);
                }
                else
                {
                    System.out.println("Unexpected Command:" + command);
                }

                while (true)
                {
                    command = scanner.nextLine();
                    String[] substrings = command.split(" ");

                    switch (substrings[0].toUpperCase())
                    {
                        case "START_TRADE":
                            long tradeTo = Integer.parseInt(substrings[1]);
                            if ( traderIDs.contains(tradeTo))
                            {
                                if (ProcessTradeRequest(tradeTo))
                                {
                                    System.out.println("\nStock has been transferred from Trader " + traderId +" to " + tradeTo+".\n");
                                    BroadcastNewStockHolder(traderId, tradeTo);
                                }
                        }
                        break;

                        default:
                            System.out.println("Unknown command: " + command + ".");
                    }
                }
            }
            catch(Exception ex)
            {
                DisconnectTrader(traderId);
                scanner.close();
            }
    }

    private void AllocateNewStockHolder(long traderId)
    {
        if (!traderIDs.isEmpty())
        {
            long newStockHolder = traderIDs.get(0);
            PrintWriter writerNew = allWriters.get(newStockHolder);
            writerNew.println("RECEIVE_TRADE");

            BroadcastNewStockHolder(traderId, newStockHolder);
            currentHolder = newStockHolder;
            System.out.println("Stock has been given to Trader" + newStockHolder+".");
            System.out.println("\nTraders currently in the market:");
            for (long l: traderIDs) {
                System.out.println("Trader " + l);
            }
        }
        else
        {
            currentHolder = 0;
        }
    }

    private void DisconnectTrader(long traderId)
    {
        try {
            System.out.println("\nTrader " + traderId + " disconnected.");

            allWriters.get(traderId).close();
            allWriters.remove(traderId);
            traderIDs.remove(traderId);

            BroadcastTraderDisconnected(traderId);

            if (currentHolder == traderId) {
                AllocateNewStockHolder(traderId);
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean ProcessTradeRequest(long newHolder)
    {

        try
        {
            PrintWriter writerNew = allWriters.get(newHolder);
            PrintWriter writerCurrent = allWriters.get(currentHolder);

            if (writerNew != null && writerCurrent != null)
            {
                writerNew.println("RECEIVE_TRADE");
                writerCurrent.println("CONFIRM_TRADE");
                writerCurrent.println(newHolder);
                currentHolder = newHolder;
                return true;
            }
            return false;
        }
        catch(Exception ex)
        {
            return false;
        }
    }

    private void BroadcastNewTraderJoined(long traderId)
    {
        for (Long id: allWriters.keySet()) {

            if (id != traderId)
            {
                PrintWriter writer = allWriters.get(id);
                writer.println("NEW_TRADER");
                writer.println(traderId);
            }
        }
    }

    private void BroadcastTraderDisconnected(long traderId)
    {
        for (Long id: allWriters.keySet()) {

        if (id != traderId)
        {
            PrintWriter writer = allWriters.get(id);
            writer.println("TRADER_DISCONNECTED");
            writer.println(traderId);
        }
    }
        PrintTradersInMarket();
    }

    private void ProcessNewConnection(long traderId)
    {
        PrintWriter writer = allWriters.get(traderId);

        writer.println(traderId);
        writer.println(currentHolder);
        writer.println(traderIDs.size());

        for (long trader : traderIDs)
        {
            writer.println(trader);
        }
        System.out.println("\nNew connection; Trader ID "+ traderId);
        PrintTradersInMarket();
    }

    private void BroadcastNewStockHolder(long traderId, long tradeTo)
    {
        for (long id : allWriters.keySet())
        {
            if (id != traderId && id != tradeTo)
            {
                PrintWriter writer = allWriters.get(id);
                writer.println("NEW_STOCKHOLDER");
                writer.println(tradeTo);
            }
        }
    }

    private void PrintTradersInMarket()
    {
        if (traderIDs.size() > 0)
        {
            System.out.println("\nTraders currently in the market:");
            for(long l : traderIDs)
            {
                System.out.println("Trader "+l);
            }
        }
        else
        {
            System.out.println("\nNo traders currently in market.");
        }
    }

    private long GetNextTraderId()
    {
        long traderId;

        synchronized (_lock) {
            nextTraderId++;
            traderId = nextTraderId;
        }
        return traderId;
    }

}
