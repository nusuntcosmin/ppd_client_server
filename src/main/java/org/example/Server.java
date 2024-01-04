package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final SyncronizedQueue queue;
    private final Thread[] threads;
    private final RankingList resultsList;
    private final int consumerThreadsNo;

    public Server(int port, int poolSize,int consumerThreadsNo,SyncronizedQueue queue, Thread[] threads, RankingList resultsList) {
        this.threads=threads;
        this.resultsList=resultsList;
        this.queue=queue;
        this.consumerThreadsNo=consumerThreadsNo;
        try {
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newFixedThreadPool(poolSize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize server", e);
        }
    }


    public void start() throws InterruptedException {
        System.out.println("Server started. Waiting for clients...");
        queue.startProducers();
        startConsumerThreads(consumerThreadsNo, threads, queue, resultsList);
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket,queue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        int port = 1234; // Portul pe care asculta serverul
        int poolSize = 10; // Numarul de threaduri din pool
        int consumerThreadsNo=2;
        Thread[] threads = new Thread[consumerThreadsNo];
        RankingList resultsList = new SyncronizedRankingList();
        SyncronizedQueue queue = new SyncronizedQueue(100);
        Server server = new Server(port, poolSize,consumerThreadsNo,queue,threads,resultsList);
        server.start();
    }

    private static void startConsumerThreads(int noConsumerThreads, Thread[] threads, SyncronizedQueue queue, RankingList resultsList) {
        for (int i = 0; i < noConsumerThreads; i++) {
            threads[i] = new ProcessingThread(queue, resultsList);
            threads[i].start();
        }
    }
}