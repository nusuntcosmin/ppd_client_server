package org.example;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Client {

    private int clientNumber;
    public Client(int clientNumber) {
        this.clientNumber = clientNumber;
    }

    public void startClient() {
        String serverAddress = "127.0.0.1";
        int port = 1234;
        int timeIntervalInSeconds = 1;

        try (Socket socket = new Socket(serverAddress, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            sendResultsToServer(out, timeIntervalInSeconds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendResultsToServer(PrintWriter out, int timeIntervalInSeconds) {
        try {

            String line;
            int pairsSent = 0;
            String batch = "";
            for (int problem = 1; problem <= 10; problem++) {
                String filePath = "input/RezultateC" + clientNumber + "_P" + problem + ".txt";
                File resultsFile = new File(filePath);
                BufferedReader fileReader = new BufferedReader(new FileReader(resultsFile));

                while ((line = fileReader.readLine()) != null) {
                    batch += line + "; ";
                    pairsSent++;

                    if (pairsSent == 20) {
                        out.println(batch);
                        TimeUnit.SECONDS.sleep(timeIntervalInSeconds);
                        pairsSent = 0;
                        batch = "";
                    }
                }

                fileReader.close();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}