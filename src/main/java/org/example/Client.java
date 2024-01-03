package org.example;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1"; // Adresa IP a serverului
        int port = 1234; // Portul la care asculta serverul
        int timeIntervalInSeconds = 5; // Intervalul de trimitere a datelor către server (Δx)

        try (Socket socket = new Socket(serverAddress, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            int clientNumber = 1;
            sendResultsToServer(out, timeIntervalInSeconds, clientNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendResultsToServer(PrintWriter out, int timeIntervalInSeconds, int clientNumber) {
        try {
            File resultsFile = new File("results.txt");
            BufferedReader fileReader = new BufferedReader(new FileReader(resultsFile));

            String line;
            int pairsSent = 0;
            String batch = "";
            for (int problem = 1; problem <= 10; problem++) {
                String filePath = "RezultateC" + clientNumber + "_P" + problem + ".txt";
            }
                while ((line = fileReader.readLine()) != null) {
                batch += line;
                pairsSent++;

                if (pairsSent == 20) {
                    out.println(batch);
                    TimeUnit.SECONDS.sleep(timeIntervalInSeconds);
                    pairsSent = 0;
                    batch = "";
                }
            }

            fileReader.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}