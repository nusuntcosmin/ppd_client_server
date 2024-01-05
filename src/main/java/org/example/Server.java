package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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


    public void start() throws InterruptedException, IOException {
        System.out.println("Server started. Waiting for clients...");
        queue.startProducers();
        startConsumerThreads(consumerThreadsNo, threads, queue, resultsList);
        int countryCount=0;
        while (true && countryCount<5) {
            try {
                countryCount++;
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket,queue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        threadPool.shutdown();
        System.out.println("Server not accepting other connections");
        var success = threadPool.awaitTermination(5, TimeUnit.MINUTES);

        queue.stopProducers();

        for (int i = 0; i < consumerThreadsNo; i++) {
            threads[i].join();
        }

        if (success) {
            List<ParticipantEntry> results = resultsList.getEntriesAsList();
            results.sort((t1, t2) -> {
                if (t1.getScore() == t2.getScore()) {
                    return t1.getId() - t2.getId();
                } else if (t1.getScore() < t2.getScore()) {
                    return 1;
                }
                return -1;
            });
            System.out.println("acum modifica fisierul");
            writeResultsToFile(results, "output\\Clasament.txt");
        };
    }

    public static void main(String[] args) throws InterruptedException, IOException {
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
    private static void writeResultsToFile(List<ParticipantEntry> resultsList, String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

        resultsList.forEach(entry -> {
            try {
                writer.write(String.valueOf(entry.getId()));
                writer.write(' ');
                writer.write(String.valueOf(entry.getScore()));
                writer.write(' ');
                writer.write(String.valueOf(entry.getCountryNum()));
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        writer.close();
    }
}