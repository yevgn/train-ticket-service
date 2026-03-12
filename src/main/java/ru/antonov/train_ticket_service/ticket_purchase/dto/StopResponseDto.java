package ru.antonov.train_ticket_service.ticket_purchase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class StopResponseDto {
    private UUID id;
    private StationResponseDto station;
    @JsonProperty("stop_order")
    private Integer stopOrder;
    @JsonProperty("planned_arrival")
    private LocalDateTime plannedArrival;
    @JsonProperty("planned_departure")
    private LocalDateTime plannedDeparture;
}
