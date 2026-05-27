package ru.antonov.trainticketservice.ticket.query.dto;

import ru.antonov.trainticketservice.ticket.query.entity.OccupiedSeatView;
import ru.antonov.trainticketservice.ticket.query.entity.TicketView;

public class DtoFactory {
    public static TicketShortDto makeTicketShortDto(TicketView entity) {
        return TicketShortDto.builder()
                .ticketId(entity.getTicketId())
                .cruiseNumber(entity.getCruiseNumber())
                .departureTime(entity.getDepartureTime())
                .arrivalTime(entity.getArrivalTime())
                .fromLocation(entity.getFromLocation())
                .toLocation(entity.getToLocation())
                .ownerId(entity.getOwnerId())
                .lastUpdate(entity.getLastUpdatedAt())
                .status(entity.getStatus().name())
                .cruiseId(entity.getCruiseId())
                .ownerEmail(entity.getOwnerEmail())
                .ownerFullName(entity.getOwnerSurname() + " " + entity.getOwnerName() + " " +
                        entity.getOwnerPatronymic())
                .build();
    }

    public static TicketDto makeTicketDto(TicketView entity) {
        return TicketDto.builder()
                .ticketId(entity.getTicketId())
                .cruiseNumber(entity.getCruiseNumber())
                .departureTime(entity.getDepartureTime())
                .arrivalTime(entity.getArrivalTime())
                .fromLocation(entity.getFromLocation())
                .toLocation(entity.getToLocation())
                .ownerId(entity.getOwnerId())
                .lastUpdate(entity.getLastUpdatedAt())
                .status(entity.getStatus().name())
                .cruiseId(entity.getCruiseId())
                .carriageCategory(entity.getCarriageCategory())
                .carriageId(entity.getCarriageId())
                .carriageNumber(entity.getCarriageNumber())
                .seatId(entity.getSeatId())
                .seatNumber(entity.getSeatNumber())
                .seatCategory(entity.getSeatCategory())
                .fare(entity.getFare())
                .fromStation(entity.getFromStation())
                .toStation(entity.getToStation())
                .ownerEmail(entity.getOwnerEmail())
                .ownerFullName(entity.getOwnerSurname() + " " + entity.getOwnerName() + " " +
                        entity.getOwnerPatronymic())
                .build();
    }

    public static OccupiedSeatDto makeOccupiedSeatDto(OccupiedSeatView entity) {
        return OccupiedSeatDto.builder()
                .ticketId(entity.getTicketId())
                .cruiseNumber(entity.getCruiseNumber())
                .cruiseId(entity.getId())
                .seatNumber(entity.getSeatNumber())
                .seatId(entity.getSeatId())
                .owner(entity.getOwner())
                .build();
    }
}
