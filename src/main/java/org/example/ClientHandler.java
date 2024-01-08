
package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final SyncronizedQueue queue;
    private final int processingThreads=4;
    private final ExecutorService threadPool;
    private final Server server;

    public ClientHandler(Socket socket,SyncronizedQueue queue,Server server) {
        this.clientSocket = socket;
        this.queue=queue;
        this.server=server;
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
                if(inputLine.equals("Clasament final")) {
                    CompletableFuture<List<ParticipantEntry>> rankingFuture = server.calculateRankingsAsync();
                    List<ParticipantEntry> rankings = rankingFuture.get();
                    returnFinalRanking(rankings, out);
                } 
                else if (inputLine.equals("Clasament pe tari")) {
                    CompletableFuture<LinkedHashMap<Integer,Integer>> countryRankingFuture = server.calculateCountryRankingsAsync();
                    LinkedHashMap<Integer,Integer> countryRankings = countryRankingFuture.get();
                    returnCountryRanking(countryRankings, out);
                }
                else {
                    for (String pair:inputLine.split("; ")) {
                        threadPool.execute(new ReadProcessHandler(this.queue, this.clientSocket.getPort(), pair));
                    }
                }
            }
            threadPool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void returnCountryRanking(LinkedHashMap<Integer, Integer> ranking,PrintWriter out) {
        ranking.entrySet().forEach(entry -> {
            out.println(entry.getKey()+" "+entry.getValue());
        });
        out.println("END");
    }
    private void returnFinalRanking(List<ParticipantEntry> ranking,PrintWriter out) {
        ranking.forEach(entry -> {
            out.println(entry.getId()+" "+entry.getScore()+" "+entry.getCountryNum());
        });
        out.println("END");
    }
}
