package com.notes.keepapi.repository;

import com.notes.keepapi.model.Note;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository
public class NoteRepository {
    private final Map<Long, Note> database = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Note save(Note note) {
        if (note.getId() == null) {
            Long newId = idGenerator.getAndIncrement();
            note.setId(newId);
            log.debug("Assigned new ID {} to note", newId);
        }
        database.put(note.getId(), note);
        log.debug("Note saved in database with ID: {}", note.getId());
        return note;
    }

    public Optional<Note> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    public List<Note> findAll() {
        return new ArrayList<>(database.values());
    }

    public void deleteById(Long id) {
        database.remove(id);
        log.debug("Note removed from database with ID: {}", id);
    }

    public boolean existsById(Long id) {
        return database.containsKey(id);
    }
}
