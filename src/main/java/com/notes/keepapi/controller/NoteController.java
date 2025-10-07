package com.notes.keepapi.controller;

import com.notes.keepapi.model.ChecklistItem;
import com.notes.keepapi.model.ItemPrediction;
import com.notes.keepapi.model.Note;
import com.notes.keepapi.service.NoteService;
import com.notes.keepapi.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;
    private final PredictionService predictionService;

    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        log.info("Creating new note with title: {}", note.getTitle());
        Note createdNote = noteService.createNote(note);
        log.info("Note created successfully with ID: {}", createdNote.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        log.info("Fetching note with ID: {}", id);
        return noteService.getNoteById(id)
                .map(note -> {
                    log.debug("Note found with ID: {}", id);
                    return ResponseEntity.ok(note);
                })
                .orElseGet(() -> {
                    log.warn("Note not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        log.info("Fetching all notes");
        List<Note> notes = noteService.getAllNotes();
        log.info("Retrieved {} notes", notes.size());
        return ResponseEntity.ok(notes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note note) {
        log.info("Updating note with ID: {}", id);
        return noteService.updateNote(id, note)
                .map(updatedNote -> {
                    log.info("Note updated successfully with ID: {}", id);
                    return ResponseEntity.ok(updatedNote);
                })
                .orElseGet(() -> {
                    log.warn("Note not found for update with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        log.info("Deleting note with ID: {}", id);
        boolean deleted = noteService.deleteNote(id);
        if (deleted) {
            log.info("Note deleted successfully with ID: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Note not found for deletion with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{noteId}/checklist/{itemId}/toggle")
    public ResponseEntity<ChecklistItem> toggleChecklistItem(
            @PathVariable Long noteId,
            @PathVariable Long itemId) {
        log.info("Toggling checklist item {} in note {}", itemId, noteId);
        return noteService.toggleChecklistItem(noteId, itemId)
                .map(item -> {
                    log.info("Checklist item {} toggled to: {}", itemId, item.isChecked());
                    return ResponseEntity.ok(item);
                })
                .orElseGet(() -> {
                    log.warn("Note or checklist item not found - noteId: {}, itemId: {}", noteId, itemId);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/{id}/guess")
    public ResponseEntity<List<ItemPrediction>> guessItemsNeeded(@PathVariable Long id) {
        log.info("Generating item predictions for note ID: {}", id);
        return noteService.getNoteById(id)
                .map(note -> {
                    List<ItemPrediction> predictions = predictionService.predictItemsNeeded(note);
                    log.info("Generated {} predictions for note ID: {}", predictions.size(), id);
                    return ResponseEntity.ok(predictions);
                })
                .orElseGet(() -> {
                    log.warn("Note not found for predictions with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }
}
