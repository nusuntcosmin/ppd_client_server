package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Server {
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final SyncronizedQueue queue;
    private final Thread[] threads;
    private final RankingList resultsList;
    private final int consumerThreadsNo;
    private boolean success;
    private long finalCalculatedTime=-1;
    private long startTime=0;
    private final int dt;
    private LinkedHashMap<Integer, Integer> currentRanking;

    public Server(int port, int poolSize,int consumerThreadsNo,SyncronizedQueue queue, Thread[] threads, RankingList resultsList,Integer dt) throws InterruptedException, IOException {
        this.threads=threads;
        this.resultsList=resultsList;
        this.queue=queue;
        this.success=false;
        this.dt=dt;
        this.consumerThreadsNo=consumerThreadsNo;
        try {
            startTime = System.currentTimeMillis();
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newFixedThreadPool(poolSize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize server", e);
        }
        start();
    }

    public CompletableFuture<List<ParticipantEntry>> calculateRankingsAsync() throws InterruptedException {
        CompletableFuture<List<ParticipantEntry>> future = new CompletableFuture<>();        
            List<ParticipantEntry> results = resultsList.getEntriesAsList();
            results.sort((t1, t2) -> {
                if (t1.getScore() == t2.getScore()) {
                    return t1.getId() - t2.getId();
                } else if (t1.getScore() < t2.getScore()) {
                    return 1;
                }
                return -1;
            });

            future.complete(results);
        return future;
    }
    public CompletableFuture<LinkedHashMap<Integer, Integer>> calculateCountryRankingsAsync() throws InterruptedException {
        CompletableFuture<LinkedHashMap<Integer, Integer>> future = new CompletableFuture<>();        
        if(finalCalculatedTime==-1 || System.currentTimeMillis()-finalCalculatedTime>this.dt) {
            List<ParticipantEntry> results = resultsList.getEntriesAsList();
            Map<Integer, Integer> totalScoresByCountry = results.stream()
                .collect(Collectors.groupingBy(ParticipantEntry::getCountryNum,
                        Collectors.summingInt(ParticipantEntry::getScore)));
            LinkedHashMap<Integer, Integer> sortedByScore = totalScoresByCountry.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            finalCalculatedTime=System.currentTimeMillis();
            this.currentRanking=sortedByScore;
            future.complete(sortedByScore);
        } else {
            future.complete(this.currentRanking);
        }
        return future;
    }

    public void start() throws InterruptedException, IOException {
        System.out.println("Server started. Waiting for clients...");
        queue.startProducers();
        startConsumerThreads(consumerThreadsNo, threads, queue, resultsList);
        int countryCount=0;
        while (true && countryCount<5) {
            try {
                countryCount++;
                Socket clientSocket = serverSocket.accept();;
                threadPool.execute(new ClientHandler(clientSocket,queue,this));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        threadPool.shutdown();
        System.out.println("Server not accepting other connections");
        success = threadPool.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("s-a terminat threadPool");
        queue.stopProducers();
        System.out.println("s-au oprit producerii");
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
            Map<Integer, Integer> totalScoresByCountry = results.stream()
                .collect(Collectors.groupingBy(ParticipantEntry::getCountryNum,
                        Collectors.summingInt(ParticipantEntry::getScore)));
            LinkedHashMap<Integer, Integer> sortedByScore = totalScoresByCountry.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            System.out.println("acum modifica fisierul");
            writeResultsToFile(sortedByScore,results, "output\\Clasament.txt","output\\ClasamentTari.txt");
            long timeToComplete=System.currentTimeMillis()-startTime;
            System.out.println(timeToComplete);
        };
    }

    private static void startConsumerThreads(int noConsumerThreads, Thread[] threads, SyncronizedQueue queue, RankingList resultsList) {
        for (int i = 0; i < noConsumerThreads; i++) {
            threads[i] = new ProcessingThread(queue, resultsList);
            threads[i].start();
        }
    }
    private static void writeResultsToFile(LinkedHashMap<Integer,Integer>countryResult, List<ParticipantEntry> resultsList, String filePath, String filePathCountry) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        BufferedWriter writerCountry = new BufferedWriter(new FileWriter(filePathCountry));
        countryResult.entrySet().forEach(entry -> {
            try {
                writerCountry.write(String.valueOf(entry.getKey()));
                writerCountry.write(' ');
                writerCountry.write(String.valueOf(entry.getValue()));
                writerCountry.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        writerCountry.close();
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