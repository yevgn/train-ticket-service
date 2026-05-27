package ru.antonov.trainticketservice.ticket.query.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupiedSeatDto {
    @JsonProperty("seat_id")
    private UUID seatId;

    @JsonProperty("seat_number")
    private String seatNumber;

    @JsonProperty("cruise_id")
    private UUID cruiseId;

    @JsonProperty("cruise_number")
    private String cruiseNumber;

    @JsonProperty("ticket_id")
    private UUID ticketId;

    private String owner;

    private UUID from;

    @JsonProperty("from_location")
    private String fromLocation;

    @JsonProperty("from_order")
    private Integer fromOrder;

    private UUID to;

    @JsonProperty("to_location")
    private String toLocation;

    @JsonProperty("to_order")
    private Integer toOrder;
}
