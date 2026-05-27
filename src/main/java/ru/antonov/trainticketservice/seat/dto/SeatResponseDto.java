package ru.antonov.trainticketservice.seat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.antonov.trainticketservice.cruise.dto.CarriageResponseDto;
import ru.antonov.trainticketservice.seat.entity.SeatCategory;

import java.util.UUID;

@Getter
@Setter
@Builder
public class SeatResponseDto {
    private UUID id;

    private String number;

    @JsonProperty("seat_category")
    private SeatCategory.Category seatCategory;

    // цена
    private Float fare;

    private CarriageResponseDto carriage;
}
