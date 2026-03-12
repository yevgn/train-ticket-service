package ru.antonov.train_ticket_service.ticket_purchase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.antonov.train_ticket_service.ticket_purchase.entity.SeatCategory;

import java.util.UUID;

@Getter
@Setter
@Builder
public class SeatResponseDto {
    private UUID id;

    private String number;

    @JsonProperty("seat_category")
    private SeatCategory.Category seatCategory;

    // наценка за расстояние и время до рейса
    private Float fare;

    private CarriageResponseDto carriage;
}
