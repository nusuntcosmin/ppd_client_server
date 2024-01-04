
package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final SyncronizedQueue queue;
    private final int processingThreads=4;
    private final ExecutorService threadPool;

    public ClientHandler(Socket socket,SyncronizedQueue queue) {
        this.clientSocket = socket;
        this.queue=queue;
        threadPool=Executors.newFixedThreadPool(processingThreads);
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                for (String pair:inputLine.split("; ")) {
                    threadPool.execute(new ReadProcessHandler(this.queue, this.clientSocket.getPort(), pair));
                }
            }
            threadPool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
