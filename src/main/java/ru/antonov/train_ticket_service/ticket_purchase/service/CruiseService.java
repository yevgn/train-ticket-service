package ru.antonov.train_ticket_service.ticket_purchase.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import ru.antonov.train_ticket_service.common.exception.*;
import ru.antonov.train_ticket_service.ticket_purchase.dto.*;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Cruise;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Stop;

import ru.antonov.train_ticket_service.ticket_purchase.repository.CruiseRepository;
import ru.antonov.train_ticket_service.user.entity.User;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CruiseService {
    private final CruiseRepository cruiseRepository;
    private final StopService stopService;

    private final CruiseAsyncService cruiseAsyncService;

    public Optional<Cruise> findById(UUID cruiseId) {
        return cruiseRepository.findById(cruiseId);
    }

    public Optional<Cruise> findByIdWithTrain(UUID cruiseId){
        return cruiseRepository.findByIdWithTrain(cruiseId);
    }

    public List<CruiseShortResponseDto> findAllCruisesWithFilter( CruiseFilterRequestDto request) {
        return cruiseRepository.findAllToDtoFilterByTargetStationsAndDateRange(
                request.getFromDate(), request.getToDate(), request.getStationFromId(), request.getStationToId()
        );
    }

    public List<CruiseShortResponseDto> findAllCruises() {
        return cruiseRepository.findAllToDto();
    }

    public CruiseResponseDto findCruiseById( UUID cruiseId) {
        Cruise cruise = cruiseRepository.findByIdFetchTrainAndStops(cruiseId).orElseThrow(
                () -> new EntityNotFoundEx(
                        "Данного рейса не существует",
                        String.format("Ошибка при поиске рейса %s: рейса не существует", cruiseId),
                        ErrorCode.ENTITY_NOT_FOUND
                )
        );

        return DtoFactory.makeCruiseResponseDto(cruise);
    }

    public void updateCruiseStop(User principal, UUID cruiseId, UUID stopId, CruiseStopUpdateRequestDto request) {
        Cruise cruise = cruiseRepository.findById(cruiseId).orElseThrow(
                () -> new EntityNotFoundEx(
                        "Данного рейса не существует",
                        String.format("Ошибка при обновлении остановки %s рейса %s " +
                                        "пользователем %s: рейса не существует",
                                stopId, cruiseId, principal.getId()),
                        ErrorCode.ENTITY_NOT_FOUND
                )
        );

        Stop stop = stopService.findByIdWithStation(stopId).orElseThrow(
                () -> new EntityNotFoundEx(
                        "Данной остановки не существует",
                        String.format("Ошибка при обновлении остановки %s рейса %s " +
                                        "пользователем %s: остановки не существует",
                                stopId, cruiseId, principal.getId()),
                        ErrorCode.ENTITY_NOT_FOUND
                )
        );

        if (!stop.getCruise().equals(cruise)) {
            throw new DataMismatchEx(
                    "Данная остановка не принадлежит этому рейсу",
                    String.format("Ошибка при обновлении остановки %s рейса %s " +
                                    "пользователем %s: остановка не принадлежит этому рейсу",
                            stopId, cruiseId, principal.getId()),
                    ErrorCode.DATA_MISMATCH
            );
        }

        // проверить не задевают ли новые arrivalTime и departureTime времена соседних остановок
        // найти предыдущую остановку
        // Найти следующуюу
        Optional<Stop> prevStopOpt = stopService.findByCruiseIdAndStopOrder(cruiseId, stop.getStopOrder() - 1);
        Optional<Stop> nextStopOpt = stopService.findByCruiseIdAndStopOrder(cruiseId, stop.getStopOrder() + 1);

        if (prevStopOpt.isPresent() && !prevStopOpt.get().getPlannedDeparture().isBefore(stop.getPlannedArrival())) {
            throw new BadRequestEx(
                    String.format("Неправильно указано новое время прибытия. Коллизия: время прибытия на указанную остановку - %s," +
                                    " время отправления с предыдущей остановки - %s", request.getNewPlannedArrival(),
                            prevStopOpt.get().getPlannedDeparture()),
                    String.format("Ошибка при обновлении остановки %s рейса %s пользователем %s. Неправильно указано " +
                                    "новое время прибытия. Коллизия: время прибытия на указанную остановку - %s," +
                                    " время отправления с предыдущей остановки - %s",
                            stopId, cruiseId, principal.getId(), request.getNewPlannedArrival(),
                            prevStopOpt.get().getPlannedDeparture()),
                    ErrorCode.BAD_REQUEST
            );
        }
        if (nextStopOpt.isPresent() && !nextStopOpt.get().getPlannedArrival().isAfter(stop.getPlannedDeparture())) {
            throw new BadRequestEx(
                    String.format("Неправильно указано новое время отправления. Коллизия: время отправления с указанной" +
                                    " остановки - %s, время прибытия на следущуюу остановку - %s", request.getNewPlannedDeparture(),
                            nextStopOpt.get().getPlannedArrival()),
                    String.format("Ошибка при обновлении остановки %s рейса %s пользователем %s. Неправильно указано" +
                                    " новое время отправления. Коллизия: время отправления с указанной остановки - %s," +
                                    " время прибытия на следущуюу остановку - %s",
                            stopId, cruiseId, principal.getId(), request.getNewPlannedDeparture(),
                            nextStopOpt.get().getPlannedArrival()),
                    ErrorCode.BAD_REQUEST
            );
        }

        LocalDateTime oldPlannedDeparture = stop.getPlannedDeparture();
        LocalDateTime oldPlannedArrival = stop.getPlannedArrival();

        stop.setPlannedArrival(request.getNewPlannedArrival());
        stop.setPlannedDeparture(request.getNewPlannedDeparture());

        try {
            stopService.save(stop);
        } catch (OptimisticLockException ex) {
            throw new DataConflictEx(
                    "Остановка была изменена другим пользователем. Повторите попытку",
                    String.format("Ошибка при обновлении остановки %s рейса %s пользователем %s: OptimisticLock",
                            stopId, cruiseId, principal.getId()),
                    ErrorCode.DATA_CONFLICT
            );
        }

        // найти людей которые купили билет с этой или до этой остановки + подгрузить станции для рассылки уведомлений
        cruiseAsyncService.findCruiseStopsAndTargetUsersAndSendCruiseStopUpdatedEvent(
                cruise, stop, oldPlannedArrival, oldPlannedDeparture
        );
    }

}
