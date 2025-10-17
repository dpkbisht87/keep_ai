package com.notes.keepapi.repository;

import com.notes.keepapi.model.PurchaseHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class PurchaseHistoryRepository {
    private final Map<Long, Map<Long, List<PurchaseHistory>>> historyStore = new ConcurrentHashMap<>();

    public PurchaseHistory save(PurchaseHistory history) {
        historyStore
                .computeIfAbsent(history.getNoteId(), key -> new ConcurrentHashMap<>())
                .computeIfAbsent(history.getItemId(), key -> Collections.synchronizedList(new ArrayList<>()))
                .add(history);

        log.debug("Stored purchase history for note {} item {}", history.getNoteId(), history.getItemId());
        return history;
    }

    public List<PurchaseHistory> findByNoteIdAndItemId(Long noteId, Long itemId) {
        return historyStore.getOrDefault(noteId, Collections.emptyMap())
                .getOrDefault(itemId, Collections.emptyList())
                .stream()
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<PurchaseHistory> findByNoteId(Long noteId) {
        return historyStore.getOrDefault(noteId, Collections.emptyMap())
                .values()
                .stream()
                .flatMap(List::stream)
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
