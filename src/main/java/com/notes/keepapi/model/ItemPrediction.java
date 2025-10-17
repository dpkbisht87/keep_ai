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
public class ItemPrediction {
    private Long itemId;
    private String itemText;
    private LocalDateTime predictedOutOfStockAt;
    private double confidence;
    private String rationale;
}
