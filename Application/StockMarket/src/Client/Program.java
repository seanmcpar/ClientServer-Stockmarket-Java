package Client;

import java.util.Scanner;

public class Program {
    public static void main(String[] args) {
        System.out.println("Press enter to connect to server...");

        try {
            Scanner in = new Scanner(System.in);
            in.nextLine();
            try (Client client = new Client())
            {
                System.out.println("Connected.");
                Runnable runnable = () -> {
                    client.RunClient();
                };
                Thread t = new Thread(runnable);
                t.start();

                while (true) {
                    var choice = in.nextLine().trim().toUpperCase();


                    switch (choice) {
                        case "T":
                            if (client.IsCurrentStockHolder()) {
                                System.out.println("Enter the ID of the trader you want to send the stock:");
                                try {
                                    int sendTo = Integer.parseInt(in.nextLine());
                                    client.Trade(sendTo);
                                }
                                catch(Exception ex){
                                    System.out.println("Not a valid number!\n");
                                    System.out.println("\nEnter T to trade stock.");
                                }
                            }
                            else
                            {
                                System.out.println("You don't have the stock.");
                            }
                            break;

                        default:
                            System.out.println("Unknown command: " + choice);
                            if (client.IsCurrentStockHolder()) {
                                System.out.println("\nEnter T to trade stock.");
                            }
                            break;
                    }
                }
            }
        }
        catch (Exception e) {
           e.printStackTrace();
        }
    }
}
