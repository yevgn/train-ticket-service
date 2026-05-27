package ru.antonov.trainticketservice.cruise.dto;

import ru.antonov.trainticketservice.cruise.entity.*;

import java.util.Comparator;


public class DtoFactory {
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
