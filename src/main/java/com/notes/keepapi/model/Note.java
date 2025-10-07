package com.notes.keepapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    private Long id;
    private String title;

    @Builder.Default
    private List<ChecklistItem> checklist = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
