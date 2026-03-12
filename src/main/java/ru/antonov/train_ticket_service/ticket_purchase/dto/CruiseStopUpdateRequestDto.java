package ru.antonov.train_ticket_service.ticket_purchase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DateTimeRangeIsCorrect(message = "Время прибытия не должно превышать время отправления")
public class CruiseStopUpdateRequestDto implements HasDateTimeRange {
    @JsonProperty("new_planned_arrival")
    @NotNull(message = "Поле new_planned_arrival не должно быть пустым")
    private LocalDateTime newPlannedArrival;

    @JsonProperty("new_planned_departure")
    @NotNull(message = "Поле new_planned_departure не должно быть пустым")
    private LocalDateTime newPlannedDeparture;

    @Override
    public LocalDateTime getStart() {
        return newPlannedArrival;
    }

    @Override
    public LocalDateTime getEnd() {
        return newPlannedDeparture;
    }
}
