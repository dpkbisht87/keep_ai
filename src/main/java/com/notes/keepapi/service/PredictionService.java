package com.notes.keepapi.service;

import com.notes.keepapi.model.ChecklistItem;
import com.notes.keepapi.model.ItemPrediction;
import com.notes.keepapi.model.PurchaseHistory;
import com.notes.keepapi.model.Note;
import com.notes.keepapi.repository.PurchaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {
    private static final Duration WARNING_LEAD_TIME = Duration.ofHours(12);

    private final PurchaseHistoryRepository purchaseHistoryRepository;

    public List<ItemPrediction> predictItemsNeeded(Note note) {
        if (note.getChecklist() == null || note.getChecklist().isEmpty()) {
            return List.of();
        }

        return note.getChecklist().stream()
                .map(item -> buildPrediction(note.getId(), item))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(ItemPrediction::getPredictedOutOfStockAt))
                .collect(Collectors.toList());
    }

    private Optional<ItemPrediction> buildPrediction(Long noteId, ChecklistItem item) {
        if (!item.isChecked()) {
            log.debug("Skipping item {} (currently unchecked) for note {}", item.getId(), noteId);
            return Optional.empty();
        }

        List<PurchaseHistory> history = purchaseHistoryRepository.findByNoteIdAndItemId(noteId, item.getId());
        if (history.isEmpty()) {
            log.debug("No purchase history for note {} item {}, unable to predict", noteId, item.getId());
            return Optional.empty();
        }

        HistorySummary summary = summarise(history);
        if (!summary.hasEnoughSamples()) {
            log.debug("Insufficient toggle pairs for note {} item {}, skipping prediction", noteId, item.getId());
            return Optional.empty();
        }

        LocalDateTime predictedOutOfStockAt = summary.lastCheckedAt.plus(summary.averageConsumptionDuration);
        LocalDateTime warnAt = predictedOutOfStockAt.minus(WARNING_LEAD_TIME);
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(warnAt)) {
            log.debug("Item {} on note {} is not near depletion yet (prediction at {})", item.getId(), noteId, predictedOutOfStockAt);
            return Optional.empty();
        }

        double confidence = summary.confidenceScore();
        String rationale = String.format("Based on %d restock cycles; average depletion in %d hours",
                summary.samples,
                summary.averageConsumptionDuration.toHours());

        return Optional.of(ItemPrediction.builder()
                .itemId(item.getId())
                .itemText(item.getText())
                .predictedOutOfStockAt(predictedOutOfStockAt)
                .confidence(confidence)
                .rationale(rationale)
                .build());
    }

    private HistorySummary summarise(List<PurchaseHistory> history) {
        List<PurchaseHistory> orderedHistory = new ArrayList<>(history);
        orderedHistory.sort(Comparator.comparing(PurchaseHistory::getTimestamp));

        LocalDateTime lastCheckedAt = null;
        List<Duration> consumptionDurations = new ArrayList<>();

        for (PurchaseHistory record : orderedHistory) {
            if (record.isCheckedState()) {
                lastCheckedAt = record.getTimestamp();
            } else if (lastCheckedAt != null) {
                Duration depletionDuration = Duration.between(lastCheckedAt, record.getTimestamp());
                if (!depletionDuration.isNegative() && !depletionDuration.isZero()) {
                    consumptionDurations.add(depletionDuration);
                }
                lastCheckedAt = null;
            }
        }

        LocalDateTime latestCheckedAt = orderedHistory.stream()
                .filter(PurchaseHistory::isCheckedState)
                .map(PurchaseHistory::getTimestamp)
                .reduce((first, second) -> second)
                .orElse(null);

        Duration averageDuration = consumptionDurations.stream()
                .reduce(Duration.ZERO, Duration::plus);

        if (!consumptionDurations.isEmpty()) {
            averageDuration = averageDuration.dividedBy(consumptionDurations.size());
        }

        return new HistorySummary(
                latestCheckedAt,
                averageDuration,
                consumptionDurations.size()
        );
    }

    private static final class HistorySummary {
        private final LocalDateTime lastCheckedAt;
        private final Duration averageConsumptionDuration;
        private final int samples;

        private HistorySummary(LocalDateTime lastCheckedAt, Duration averageConsumptionDuration, int samples) {
            this.lastCheckedAt = lastCheckedAt;
            this.averageConsumptionDuration = averageConsumptionDuration;
            this.samples = samples;
        }

        private boolean hasEnoughSamples() {
            return lastCheckedAt != null && samples > 0 && !averageConsumptionDuration.isZero();
        }

        private double confidenceScore() {
            // simple heuristic: clamp between 0.3 and 0.95 based on sample count
            double base = 0.3 + (0.15 * samples);
            return Math.min(0.95, base);
        }
    }
}
