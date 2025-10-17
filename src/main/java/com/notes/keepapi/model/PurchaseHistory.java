package com.notes.keepapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseHistory {
    private Long noteId;
    private Long itemId;
    private String itemText;
    private boolean checkedState;
    private LocalDateTime timestamp;
}
