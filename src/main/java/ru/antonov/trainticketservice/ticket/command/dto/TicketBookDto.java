package ru.antonov.trainticketservice.ticket.command.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketBookDto {
    @JsonProperty("ticket_id")
    private UUID ticketId;

    @JsonProperty("seat_id")
    private UUID seatId;

    @JsonProperty("stop_from_id")
    private UUID stopFromId;

    @JsonProperty("stop_to_id")
    private UUID stopToId;
}
