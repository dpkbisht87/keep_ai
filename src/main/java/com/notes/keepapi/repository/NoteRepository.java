package com.notes.keepapi.repository;

import com.notes.keepapi.model.Note;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class NoteRepository {
    private final Map<Long, Note> database = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Note save(Note note) {
        if (note.getId() == null) {
            note.setId(idGenerator.getAndIncrement());
        }
        database.put(note.getId(), note);
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
    }

    public boolean existsById(Long id) {
        return database.containsKey(id);
    }
}
