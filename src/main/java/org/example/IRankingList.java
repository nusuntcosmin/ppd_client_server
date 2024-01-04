package org.example;

import java.util.List;

public interface IRankingList {
    void processParticipantEntry(ParticipantEntry entry);
    List<ParticipantEntry> getEntriesAsList();
}
