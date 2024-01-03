package org.example;

public class Main {
    public static void main(String[] args) {
        // Define the number of clients to run
        int numberOfClients = 5;

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