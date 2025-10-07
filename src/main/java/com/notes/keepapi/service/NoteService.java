package com.notes.keepapi.service;

import com.notes.keepapi.model.ChecklistItem;
import com.notes.keepapi.model.Note;
import com.notes.keepapi.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final AtomicLong checklistItemIdGenerator = new AtomicLong(1);

    public Note createNote(Note note) {
        LocalDateTime now = LocalDateTime.now();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);

        // Assign IDs to checklist items
        if (note.getChecklist() != null) {
            note.getChecklist().forEach(item -> {
                if (item.getId() == null) {
                    item.setId(checklistItemIdGenerator.getAndIncrement());
                }
            });
        }

        return noteRepository.save(note);
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Optional<Note> updateNote(Long id, Note updatedNote) {
        return noteRepository.findById(id).map(existingNote -> {
            existingNote.setTitle(updatedNote.getTitle());

            // Update checklist items
            if (updatedNote.getChecklist() != null) {
                // Assign IDs to new checklist items
                updatedNote.getChecklist().forEach(item -> {
                    if (item.getId() == null) {
                        item.setId(checklistItemIdGenerator.getAndIncrement());
                    }
                });
                existingNote.setChecklist(updatedNote.getChecklist());
            }

            existingNote.setUpdatedAt(LocalDateTime.now());
            return noteRepository.save(existingNote);
        });
    }

    public boolean deleteNote(Long id) {
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<ChecklistItem> toggleChecklistItem(Long noteId, Long itemId) {
        return noteRepository.findById(noteId).flatMap(note -> {
            Optional<ChecklistItem> itemOpt = note.getChecklist().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst();

            itemOpt.ifPresent(item -> {
                item.setChecked(!item.isChecked());
                note.setUpdatedAt(LocalDateTime.now());
                noteRepository.save(note);
            });

            return itemOpt;
        });
    }
}
