package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

//how to run: java -cp out org.example.Client 6767

public class Client {
    public static void main(String[] args) {
        if(args.length < 1)
        {
            System.out.println("Error: specify the server port!");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException e) {
            System.out.println("Error: specify the server port!");
            return;
        }

        try (
            Socket socket = new Socket("127.0.0.1", port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        )
        {
            System.out.println("Successfully connected to server!");

            readServerData(in);

            while(true)
            {
                System.out.print("> ");
                String userInput = scanner.nextLine();

                out.println(userInput);

                if(userInput.trim().equals("exit") || userInput.trim().equals("5"))
                {
                    System.out.println("Disconnecting!");
                    break;
                }

                readServerData(in);

                System.out.print("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        }
        catch(Exception e)
        {
            System.out.println("Error!");
        }
    }

    public static void readServerData(BufferedReader in) throws IOException
    {
        String line;
        while((line = in.readLine()) != null)
        {
            if(line.equals("EOF") || line.equals("Server closing!")) {
                break;
            }
            System.out.println(line);
        }
    }
}
