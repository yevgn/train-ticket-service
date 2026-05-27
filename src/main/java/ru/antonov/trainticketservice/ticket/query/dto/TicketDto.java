package ru.antonov.trainticketservice.ticket.query.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketDto {
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

    @JsonProperty("from_stop_id")
    private UUID fromStopId;

    @JsonProperty("from_location")
    private String fromLocation;

    @JsonProperty("from_station")
    private String fromStation;

    @JsonProperty("departure_time")
    private LocalDateTime departureTime;

    @JsonProperty("to_stop_id")
    private UUID toStopId;

    @JsonProperty("to_location")
    private String toLocation;

    @JsonProperty("to_station")
    private String toStation;

    @JsonProperty("arrival_time")
    private LocalDateTime arrivalTime;

    @JsonProperty("seat_id")
    private UUID seatId;

    @JsonProperty("seat_number")
    private String seatNumber;

    @JsonProperty("seat_category")
    private String seatCategory;

    @JsonProperty("carriage_id")
    private UUID carriageId;

    @JsonProperty("carriage_number")
    private Integer carriageNumber;

    @JsonProperty("carriage_category")
    private String carriageCategory;

    private String status;

    private Float fare;

    @JsonProperty("last_update")
    private LocalDateTime lastUpdate;
}
