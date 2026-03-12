package ru.antonov.train_ticket_service.ticket_purchase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Ticket;
import ru.antonov.train_ticket_service.user.dto.UserResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TicketResponseDto {
    private UUID id;

    @JsonProperty("purchased_at")
    private LocalDateTime purchasedAt;

    private Ticket.Status status;

    private Float fare;

    private CruiseShortResponseDto cruise;

    private UserResponseDto user;

    private SeatResponseDto seat;

    private StopResponseDto from;

    private StopResponseDto to;
}
