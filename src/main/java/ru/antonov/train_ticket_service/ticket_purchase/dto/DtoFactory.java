package ru.antonov.train_ticket_service.ticket_purchase.dto;

import ru.antonov.train_ticket_service.ticket_purchase.entity.*;

import java.util.Comparator;

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

    public static CruiseShortResponseDto makeCruiseShortResponseDto(Cruise cruise, Stop from, Stop to){
        return CruiseShortResponseDto
                .builder()
                .id(cruise.getId())
                .number(cruise.getNumber())
                .from(makeStopResponseDto(from))
                .to(makeStopResponseDto(to))
                .status(cruise.getStatus())
                .train(makeTrainResponseDto(cruise.getTrain()))
                .build();
    }

    public static CarriageResponseDto makeCarriageResponseDto(Carriage carriage){
        return CarriageResponseDto
                .builder()
                .carriageId(carriage.getId())
                .number(carriage.getNumber())
                .category(carriage.getCategory().getCategory())
                .build();
    }

    public static TrainResponseDto makeTrainResponseDto(Train train){
        return TrainResponseDto
                .builder()
                .id(train.getId())
                .category(train.getCategory().getCategory())
                .build();
    }

    public static StationResponseDto makeStationResponseDto(Station station){
        return StationResponseDto
                .builder()
                .id(station.getId())
                .name(station.getName())
                .location(station.getLocation())
                .build();
    }

    public static TicketResponseDto makeTicketResponseDto(Ticket ticket, Stop cruiseStart, Stop cruiseEnd){
        return TicketResponseDto
                .builder()
                .id(ticket.getId())
                .cruise(makeCruiseShortResponseDto(ticket.getCruise(), cruiseStart, cruiseEnd))
                .from(makeStopResponseDto(ticket.getFrom()))
                .to(makeStopResponseDto(ticket.getTo()))
                .status(ticket.getStatus())
                .purchasedAt(ticket.getPurchasedAt())
                .seat(makeSeatResponseDto(ticket.getSeat(), ticket.getActualFare()))
                .user(ru.antonov.train_ticket_service.user.dto.DtoFactory.makeUserResponseDto(ticket.getUser()))
                .fare(ticket.getActualFare())
                .build();
    }

    public static TicketShortResponseDto makeTicketShortResponseDto(Ticket ticket){
        return TicketShortResponseDto
                .builder()
                .id(ticket.getId())
                .cruiseNumber(ticket.getCruise().getNumber())
                .departureTime(ticket.getFrom().getPlannedDeparture())
                .arrivalTime(ticket.getTo().getPlannedArrival())
                .stopFromLocation(ticket.getFrom().getStation().getLocation())
                .stopToLocation(ticket.getTo().getStation().getLocation())
                .status(ticket.getStatus())
                .purchasedAt(ticket.getPurchasedAt())
                .fare(ticket.getActualFare())
                .build();
    }

    public static CruiseResponseDto makeCruiseResponseDto(Cruise cruise){
        return CruiseResponseDto
                .builder()
                .id(cruise.getId())
                .number(cruise.getNumber())
                .train(makeTrainResponseDto(cruise.getTrain()))
                .status(cruise.getStatus())
                .stops(
                        cruise.getStops().stream()
                                .sorted(Comparator.comparing(Stop::getStopOrder))
                                .map(DtoFactory::makeStopResponseDto)
                                .toList()
                )
                .build();
    }

    public static StopResponseDto makeStopResponseDto(Stop stop){
        return StopResponseDto
                .builder()
                .id(stop.getId())
                .station(makeStationResponseDto(stop.getStation()))
                .stopOrder(stop.getStopOrder())
                .plannedArrival(stop.getPlannedArrival())
                .plannedDeparture(stop.getPlannedDeparture())
                .build();
    }

}
