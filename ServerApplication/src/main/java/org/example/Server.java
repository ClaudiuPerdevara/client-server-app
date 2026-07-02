package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// how to run: java -cp out org.example.Server 6767

public class Server
{
    public static int PORT;
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public Server(int port)
    {
        try
        {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
        }
        catch(IOException e)
        {
            if(running)
                System.out.println(e.getMessage());
        }
    }

    public void start()
    {
        try
        {
            while(running)
            {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client accepted!");
                threadPool.execute(new ClientThread(clientSocket, this));
            }
        }
        catch(IOException e)
        {
            if(running)
                System.out.println(e.getMessage());
        }
        finally
        {
            stopServer();
        }
    }

    public void stopServer()
    {
        if(!running)
            return;
        running = false;
        System.out.println("Starting shutdown");

        try
        {
            threadPool.shutdown();
            if(serverSocket != null)
                serverSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: You have to specify a port number!");
            return;
        }

        try
        {
            int port = Integer.parseInt(args[0]);
            Server server = new Server(port);
            server.start();
        }
        catch (NumberFormatException e)
        {
            System.out.println("Error: Port number must be an integer!");
        }

    }
}
