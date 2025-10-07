package com.notes.keepapi.service;

import com.notes.keepapi.model.ChecklistItem;
import com.notes.keepapi.model.Note;
import com.notes.keepapi.model.PurchaseHistory;
import com.notes.keepapi.repository.NoteRepository;
import com.notes.keepapi.repository.PurchaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final AtomicLong checklistItemIdGenerator = new AtomicLong(1);

    public Note createNote(Note note) {
        log.debug("Creating note with title: {}", note.getTitle());
        LocalDateTime now = LocalDateTime.now();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);

        // Assign IDs to checklist items
        if (note.getChecklist() != null) {
            log.debug("Assigning IDs to {} checklist items", note.getChecklist().size());
            note.getChecklist().forEach(item -> {
                if (item.getId() == null) {
                    item.setId(checklistItemIdGenerator.getAndIncrement());
                }
            });
        }

        Note savedNote = noteRepository.save(note);
        log.debug("Note saved with ID: {}", savedNote.getId());
        return savedNote;
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Optional<Note> updateNote(Long id, Note updatedNote) {
        log.debug("Updating note with ID: {}", id);
        return noteRepository.findById(id).map(existingNote -> {
            existingNote.setTitle(updatedNote.getTitle());

            // Update checklist items
            if (updatedNote.getChecklist() != null) {
                log.debug("Updating {} checklist items for note ID: {}", updatedNote.getChecklist().size(), id);
                // Assign IDs to new checklist items
                updatedNote.getChecklist().forEach(item -> {
                    if (item.getId() == null) {
                        item.setId(checklistItemIdGenerator.getAndIncrement());
                    }
                });
                existingNote.setChecklist(updatedNote.getChecklist());
            }

            existingNote.setUpdatedAt(LocalDateTime.now());
            Note savedNote = noteRepository.save(existingNote);
            log.debug("Note updated successfully with ID: {}", id);
            return savedNote;
        });
    }

    public boolean deleteNote(Long id) {
        log.debug("Attempting to delete note with ID: {}", id);
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
            log.debug("Note deleted with ID: {}", id);
            return true;
        }
        log.debug("Note not found for deletion with ID: {}", id);
        return false;
    }

    public Optional<ChecklistItem> toggleChecklistItem(Long noteId, Long itemId) {
        log.debug("Toggling checklist item {} in note {}", itemId, noteId);
        return noteRepository.findById(noteId).flatMap(note -> {
            Optional<ChecklistItem> itemOpt = note.getChecklist().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst();

            itemOpt.ifPresent(item -> {
                boolean newState = !item.isChecked();
                item.setChecked(newState);
                log.debug("Checklist item {} checked state changed to: {}", itemId, newState);

                // Record purchase history
                recordPurchaseHistory(noteId, item, newState);

                note.setUpdatedAt(LocalDateTime.now());
                noteRepository.save(note);
            });

            if (itemOpt.isEmpty()) {
                log.debug("Checklist item {} not found in note {}", itemId, noteId);
            }

            return itemOpt;
        });
    }

    /**
     * Records purchase history when an item is checked/unchecked
     */
    private void recordPurchaseHistory(Long noteId, ChecklistItem item, boolean checkedState) {
        PurchaseHistory history = PurchaseHistory.builder()
                .noteId(noteId)
                .itemId(item.getId())
                .itemText(item.getText())
                .checkedState(checkedState)
                .timestamp(LocalDateTime.now())
                .build();

        purchaseHistoryRepository.save(history);
        log.debug("Recorded purchase history for item '{}' - checked: {}", item.getText(), checkedState);
    }
}
