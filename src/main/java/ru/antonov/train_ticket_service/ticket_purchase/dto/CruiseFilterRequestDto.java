package ru.antonov.train_ticket_service.ticket_purchase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DateTimeRangeIsCorrect(message = "Дата \"от\" не должна превышать дату \"до\"")
public class CruiseFilterRequestDto implements HasDateTimeRange {
    @JsonProperty("from_date")
    @NotNull(message = "Поле from_date не может отсутствовать")
    private LocalDateTime fromDate;

    @JsonProperty("to_date")
    @NotNull(message = "Поле to_date не может отсутствовать")
    private LocalDateTime toDate;

    @NotNull(message = "Поле station_from_id не может отсутствовать")
    @JsonProperty("station_from_id")
    private UUID stationFromId;

    @NotNull(message = "Поле station_to_id не может отсутствовать")
    @JsonProperty("station_to_id")
    private UUID stationToId;

    @Override
    public LocalDateTime getStart() {
        return fromDate;
    }

    @Override
    public LocalDateTime getEnd() {
        return toDate;
    }
}
