package org.example;


public class ProcessingThread extends Thread {
    private final SyncronizedQueue queue;
    private final RankingList resultList;

    public ProcessingThread(SyncronizedQueue queue, RankingList resultList) {
        this.queue = queue;
        this.resultList = resultList;
    }

    @Override
    public void run() {
        queue.startConsumer();

        while (true) {
            try {
                var entry = queue.dequeue();

                if (entry == null) {
                    break;
                }

                resultList.processParticipantEntry(entry);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        queue.stopConsumer();
    }
}
