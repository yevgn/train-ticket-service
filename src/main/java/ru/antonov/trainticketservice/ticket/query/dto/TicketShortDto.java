package ru.antonov.trainticketservice.ticket.query.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketShortDto {
    @JsonProperty("ticket_id")
    private UUID ticketId;

    @JsonProperty("owner_id")
    private UUID ownerId;

    @JsonProperty("owner_email")
    private String ownerEmail;

    @JsonProperty("owner_full_name")
    private String ownerFullName;

    @JsonProperty("cruise_id")
    private UUID cruiseId;

    @JsonProperty("cruise_number")
    private String cruiseNumber;

    @JsonProperty("from_location")
    private String fromLocation;

    @JsonProperty("to_location")
    private String toLocation;

    @JsonProperty("departure_time")
    private LocalDateTime departureTime;

    @JsonProperty("arrival_time")
    private LocalDateTime arrivalTime;

    private String status;

    @JsonProperty("last_update")
    private LocalDateTime lastUpdate;
}
