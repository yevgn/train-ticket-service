package ru.antonov.train_ticket_service.ticket_purchase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Ticket;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketShortResponseDto {
    private UUID id;

    @JsonProperty("purchased_at")
    private LocalDateTime purchasedAt;

    private Ticket.Status status;

    private Float fare;

    @JsonProperty("departure_time")
    private LocalDateTime departureTime;

    @JsonProperty("arrival_time")
    private LocalDateTime arrivalTime;

    @JsonProperty("stop_from_location")
    private String stopFromLocation;

    @JsonProperty("stop_to_location")
    private String stopToLocation;

    @JsonProperty("cruise_number")
    private String cruiseNumber;
}
