package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Define the number of clients to run
        int numberOfClients = 5;
        Thread serverThread = new Thread(()->{
            int port = 1234; // Portul pe care asculta serverul
            int poolSize = 10; // Numarul de threaduri din pool
            int consumerThreadsNo=2;
            int dt=2;
            Thread[] threads = new Thread[consumerThreadsNo];
            RankingList resultsList = new SyncronizedRankingList();
            SyncronizedQueue queue = new SyncronizedQueue(100);
            try {
                Server server = new Server(port, poolSize,consumerThreadsNo,queue,threads,resultsList,dt);
            } catch (InterruptedException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        serverThread.start();
        // Create and start threads for each client
        for (int i = 1; i <= numberOfClients; i++) {
            final int clientNumber = i;
            Thread clientThread = new Thread(() -> {
                Client client = new Client(clientNumber);
                client.startClient();
            });

            clientThread.start();
        }
    }
}