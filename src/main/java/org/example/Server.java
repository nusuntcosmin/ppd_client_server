package org.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;

    public Server(int port, int poolSize) {
        try {
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newFixedThreadPool(poolSize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize server", e);
        }
    }

    public void start() {
        System.out.println("Server started. Waiting for clients...");

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                InetAddress clientAddress = clientSocket.getInetAddress();
                System.out.println("Client connected: " + clientAddress + clientSocket);

                // Handle each client connection in a separate thread
                threadPool.execute(new ClientHandler(clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int port = 1234; // Portul pe care asculta serverul
        int poolSize = 10; // Numarul de threaduri din pool

        Server server = new Server(port, poolSize);
        server.start();
    }
}