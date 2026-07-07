package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

//how to run: java -cp out org.example.Client 6767

public class Client {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Command syntax: java Client <SERVER_PORT> [COMMAND_INDEX] [ARGUMENTS...]");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Error: specify the server port!");
            return;
        }

        try (
                Socket socket = new Socket("127.0.0.1", port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            if (args.length > 1)
            {
                readServerData(in, false);
                String commandIndex = args[1];

                if (commandIndex.equals("4")) {
                    if (args.length < 3) {
                        System.out.println("Error: Please provide the absolute path to the zip file as the 3rd argument.");
                        out.println("exit");
                        return;
                    }
                    String zipFilePath = args[2];

                    try {
                        byte[] bytes = Files.readAllBytes(Paths.get(zipFilePath));

                        out.println("4 " + bytes.length);

                        String ack = in.readLine();
                        if (ack != null && ack.equals("ACK")) {
                            socket.getOutputStream().write(bytes);
                            socket.getOutputStream().flush();
                        } else {
                            System.out.println("Error: Server not ready to send zip!");
                        }
                    } catch (Exception e) {
                        System.out.println("Error reading the ZIP file: " + e.getMessage());
                    }
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        sb.append(args[i]);
                        if (i < args.length - 1) {
                            sb.append(" ");
                        }
                    }
                    out.println(sb.toString());
                }

                readServerData(in, true);

                out.println("exit");
            }
            //INTERACTIVE MODE
            else
            {
                System.out.println("Successfully connected to server!");

                readServerData(in, true);

                while (true)
                {
                    System.out.print("> ");
                    String userInput = scanner.nextLine().trim();
                    String message = userInput;

                    if (userInput.equals("3")) {
                        System.out.println("Please write a location: ");
                        String location = scanner.nextLine();

                        message = message + " " + location;
                        out.println(message);
                    } else if (userInput.equals("4")) {
                        System.out.println("Please write the absolute path to a ZIP file: ");
                        String zipPath = scanner.nextLine();

                        try {
                            byte[] bytes = Files.readAllBytes(Paths.get(zipPath));

                            out.println("4 " + bytes.length);

                            String ack = in.readLine(); //waiting for ACK signal from server

                            if (ack != null && ack.equals("ACK")) {
                                socket.getOutputStream().write(bytes);
                                socket.getOutputStream().flush();
                                System.out.println("Uploading file ");
                            } else {
                                System.out.println("Error: Server not ready to send zip!");
                            }
                        } catch (Exception e) {
                            System.out.println("Error reading the ZIP file: " + e.getMessage());
                            continue;
                        }
                    } else {
                        out.println(message);
                    }

                    if (userInput.trim().equals("exit") || userInput.trim().equals("5")) {
                        System.out.println("Disconnecting!");
                        break;
                    }

                    readServerData(in, true);

                    System.out.print("\nPress ENTER to continue...");
                    scanner.nextLine();
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void readServerData(BufferedReader in, boolean print) throws IOException
    {
        String line;
        while((line = in.readLine()) != null)
        {
            if(line.equals("EOF") || line.equals("Server closing!")) {
                break;
            }
            if(print) {
                System.out.println(line);
            }
        }
    }
}