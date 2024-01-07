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
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sendResultsToServer(in,out, timeIntervalInSeconds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendResultsToServer(BufferedReader in,PrintWriter out, int timeIntervalInSeconds) {
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
            System.out.println("Clientul "+this.clientNumber+" a cerut clasamentul final");
            out.println("Clasament final");
            String[] clasament = new String[1000];
            Integer lineNumber = 0;
            System.out.println("Clientul "+this.clientNumber+" citeste clasamentul final");
            while((line=in.readLine()) !=null) {
                if(line.equals("END")) break;
                clasament[lineNumber]=line;
                lineNumber++;
            }
            System.out.println(lineNumber);
            System.out.println("Clientul "+this.clientNumber+" a primit "+lineNumber+" participanti");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}