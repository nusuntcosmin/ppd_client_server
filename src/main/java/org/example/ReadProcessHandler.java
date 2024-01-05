package org.example;

public class ReadProcessHandler implements Runnable {
    private final SyncronizedQueue queue;
    private final int socket;
    private final String pair;

    public ReadProcessHandler(SyncronizedQueue queue,int socket, String pair){
        this.queue=queue;
        this.socket=socket;
        this.pair=pair;
    }

    @Override
    public void run() {
        queue.enqueue(new ParticipantEntry(Integer.parseInt(pair.split(" ")[0]), Integer.parseInt(pair.split(" ")[1]), socket));
    }
    
}
