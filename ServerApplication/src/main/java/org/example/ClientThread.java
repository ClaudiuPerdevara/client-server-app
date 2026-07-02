package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;



public class ClientThread extends Thread {
    private final Socket socket;
    private final Server server;

    public ClientThread(Socket socket, Server server)
    {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try(
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); /*auto flush activated */ )
        {
            menu(out);

            String clientMessage;
            while((clientMessage = in.readLine()) != null)
            {
                clientMessage = clientMessage.toLowerCase().trim();
                if(clientMessage.equals("exit"))
                {
                    out.println("Server closing!");
                    break;
                }

                processCommand(clientMessage, out);
            }


        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally { //channels are closed, but i have to make sure I also close the socket
            try
            {
                if(socket != null && !socket.isClosed())
                {
                    socket.close();
                    System.out.println("Socket closed");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    private void menu(PrintWriter out)
    {
        out.println("Please write the wished option number:");
        out.println("1. Show date & time");
        out.println("2. Show OS info");
        out.println("3. Show location based weather");
        out.println("4. Compile source code");
        out.println("5. Exit");
        out.println("EOF");
    }

    private void processCommand(String command,  PrintWriter out)
    {
        System.out.println("Server command received: " + command);

        out.println("Server is processing command: " + command);

        out.println("EOF");
    }
}
