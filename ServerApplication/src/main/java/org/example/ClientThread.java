package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


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

        switch (command)
        {
            case "1":
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                out.println("Server Date & Time: "+ now.format(myFormat));

            case "2":
                out.println("Server OS info: ");
                String osLine = runBashCommand("grep PRETTY_NAME /etc/os-release");
                out.println("OS: " + osLine.replace("PRETTY_NAME=", "").replace("\"", ""));

                String uptimeLine = runBashCommand("uptime -p");
                out.println("Uptime: " + uptimeLine);

                try
                {
                    for(String line : Files.readAllLines(Paths.get("/proc/cpuinfo")))
                    {
                        if(line.startsWith("model name"))
                        {
                            out.println("CPU: " + line.split(":")[1].trim());
                            break;
                        }
                    }
                }
                catch(Exception e)
                {
                    out.println("CPU: " + e.getMessage());
                }

                try {
                    Scanner s = new Scanner(runBashCommand("free -h"));
                    while(s.hasNextLine())
                    {
                        out.println(s.nextLine());
                    }
                }
                catch(Exception e) {
                    out.println("CPU: " + e.getMessage());
            }
        }

        out.println("EOF");
    }

    private String runBashCommand(String command)
    {
        StringBuilder sb = new StringBuilder();

        try
        {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }

            process.waitFor();
        }
        catch(Exception e)
        {
            return "Read error in runBashCommand: " + e.getMessage();
        }
        return sb.toString().trim();
    }
}
