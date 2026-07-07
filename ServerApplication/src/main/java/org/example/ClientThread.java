package org.example;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
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
        String [] args = command.split(" ",2);
        String commandIndex = args[0];
        switch (commandIndex)
        {
            case "1":
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                out.println("Server Date & Time: "+ now.format(myFormat));
                break;

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
                break;

            case "3":

                if(args.length < 2 || args[1].trim().length() == 0)
                {
                    out.println("Error: No location specified");
                    break;
                }

                String location = args[1].trim();

                try
                {
                    String formattedLocation = location.replace(" ", "%20");
                    String geoURL = "https://geocoding-api.open-meteo.com/v1/search?name=" + formattedLocation + "&count=1";

                    String geoResponse = makeHttpRequest(geoURL); // to do : implement this function!!!!!!

                    if(!geoResponse.contains("\"results\""))
                    {
                        out.println("Error: No information found");
                        break;
                    }

                    String lat = extractJsonValue(geoResponse, "\"latitude\":");
                    String lon = extractJsonValue(geoResponse, "\"longitude\":");

                    String weatherURL = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current_weather=true";
                    String weatherResponse = makeHttpRequest(weatherURL);

                    int blockIndex = weatherResponse.indexOf("\"current_weather\":");
                    if (blockIndex != -1)
                        weatherResponse = weatherResponse.substring(blockIndex);

                    String temperature = extractJsonValue(weatherResponse, "\"temperature\":");

                    out.println("Location: " + formattedLocation);
                    out.println("Temperature: " + temperature);
                }
                catch(Exception e)
                {
                    out.println("Error: " + e.getMessage());
                }

                System.out.println(" ------------------------------ ");
                break;

            case "4":

                if(args.length < 2)
                {
                    out.println("Error: No file size specified");
                    break;
                }

                out.println("ACK");

                try{
                    int fileSize = Integer.parseInt(args[1]);
                    byte[] zipBytes = new byte[fileSize];

                    InputStream is = socket.getInputStream();
                    int totalRead = 0;

                    while(totalRead < fileSize)
                    {
                        int bytesRead = is.read(zipBytes, totalRead, fileSize-totalRead);
                        if(bytesRead == -1)
                            break;

                        totalRead += bytesRead;
                    }

                    //i have read the bytes from the file
                    //now i'll save the file in a tempDir
                    Path tempDir = Files.createTempDirectory("server_");
                    Path zipPath = tempDir.resolve("source.zip");
                    Files.write(zipPath, zipBytes);

                    String pathAbsolute = tempDir.toAbsolutePath().toString();

                    runBashCommand("cd " + pathAbsolute + " && unzip -o -q source.zip"); // unzipping

                    boolean isJava = false;
                    boolean isC = false;
                    boolean isCpp = false;
                    boolean isPython = false;

                    File folder = tempDir.toFile();
                    File[] files = folder.listFiles();
                    String pythonScript = "";

                    if(files != null)
                    {
                        for(File f : files)
                        {
                            String fileName = f.getName();
                            if(fileName.toLowerCase().endsWith(".py")) {
                                isPython = true;
                                pythonScript = f.getName();
                            }
                            else if(fileName.toLowerCase().endsWith(".cpp")) isCpp = true;
                            else if(fileName.toLowerCase().endsWith(".java")) isJava = true;
                            else if(fileName.toLowerCase().endsWith(".c")) isC = true;
                        }
                    }

                    String commandToExecute = new String();
                    boolean executed = false;

                    if(isJava)
                    {
                        commandToExecute = "javac *.java 2>&1 && java Main 2>&1";
                        String linuxResult = runBashCommand("cd "+ pathAbsolute + " && " +  commandToExecute);
                        executed = true;
                        if(linuxResult.isEmpty())
                        {
                            out.println("Java program executed. No output returned.");
                        }
                        else
                        {
                            out.println(linuxResult);
                        }
                    }

                    if(isC)
                    {
                        commandToExecute = "gcc *.c -o program 2>&1 && ./program 2>&1";
                        String linuxResult = runBashCommand("cd "+ pathAbsolute + " && " +  commandToExecute);
                        executed = true;
                        if(linuxResult.isEmpty())
                        {
                            out.println("C program executed. No output returned.");
                        }
                        else
                        {
                            out.println(linuxResult);
                        }
                    }

                    if(isCpp)
                    {
                        commandToExecute = "g++ *.cpp -o program 2>&1 && ./program 2>&1";
                        String linuxResult = runBashCommand("cd "+ pathAbsolute + " && " +  commandToExecute);
                        executed = true;
                        if(linuxResult.isEmpty())
                        {
                            out.println("C++ program executed. No output returned.");
                        }
                        else
                        {
                            out.println(linuxResult);
                        }
                    }

                    if(isPython)
                    {
                        commandToExecute = "python3 " + pythonScript + " 2>&1";
                        String linuxResult = runBashCommand("cd "+ pathAbsolute + " && " +  commandToExecute);
                        executed = true;
                        if(linuxResult.isEmpty())
                        {
                            out.println("Python program executed. No output returned.");
                        }
                        else
                        {
                            out.println(linuxResult);
                        }
                    }

                    if(!executed)
                    {
                        out.println("Error: No valid file to compile found");
                        break;
                    }

                    runBashCommand("rm -rf " + pathAbsolute);
                }
                catch (Exception e)
                {
                    out.println("Error: " + e.getMessage());
                }

                break;
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

    private String extractJsonValue(String json, String key)
    {
        int keyIndex = json.indexOf(key);
        if(keyIndex == -1) return "Nothing";

        int startIndex = keyIndex + key.length();
        int endIndex = json.indexOf(",", startIndex);

        if(endIndex == -1)
            endIndex = json.indexOf("}", startIndex);

        if(endIndex == -1) return "Nothing";

        return json.substring(startIndex, endIndex).trim();
    }

    private String  makeHttpRequest(String url) throws Exception
    {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
