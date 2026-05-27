package ru.antonov.trainticketservice.ticket.command.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCancelDto {
    @JsonProperty("ticket_id")
    @NotNull(message = "Поле ticket_id не должно отсутствовать")
    private UUID ticketId;
}
