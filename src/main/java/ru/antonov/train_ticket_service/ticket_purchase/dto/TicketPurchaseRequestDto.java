package ru.antonov.train_ticket_service.ticket_purchase.dto;

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
public class TicketPurchaseRequestDto {
    @JsonProperty("seat_id")
    @NotNull(message = "Поле seat_id не должно отсутствовать")
    private UUID seatId;

    @JsonProperty("stop_from_id")
    @NotNull(message = "Поле stop_from_id не должно отсутствовать")
    private UUID stopFromId;

    @JsonProperty("stop_to_id")
    @NotNull(message = "Поле stop_to_id не должно отсутствовать")
    private UUID stopToId;
}
