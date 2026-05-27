package ru.antonov.trainticketservice.seat.dto;


import ru.antonov.trainticketservice.seat.entity.Seat;

import static ru.antonov.trainticketservice.cruise.dto.DtoFactory.makeCarriageResponseDto;

public class DtoFactory {
    public static SeatResponseDto makeSeatResponseDto(Seat seat, Float fare){
        return SeatResponseDto
                .builder()
                .id(seat.getId())
                .number(seat.getNumber())
                .carriage(makeCarriageResponseDto(seat.getCarriage()))
                .seatCategory(seat.getCategory().getCategory())
                .fare(fare)
                .build();
    }

}
