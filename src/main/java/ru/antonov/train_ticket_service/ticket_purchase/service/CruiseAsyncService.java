package ru.antonov.train_ticket_service.ticket_purchase.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.antonov.kafka.events.CruiseUpdated;
import ru.antonov.train_ticket_service.common.exception.ErrorCode;
import ru.antonov.train_ticket_service.common.exception.InternalServerException;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Cruise;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Stop;
import ru.antonov.train_ticket_service.ticket_purchase.message_producer.cruise_updated.CruiseUpdatedProducer;
import ru.antonov.train_ticket_service.ticket_purchase.repository.CruiseRepository;
import ru.antonov.train_ticket_service.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CruiseAsyncService {
    private final StopService stopService;
    private final CruiseRepository cruiseRepository;

    private final CruiseUpdatedProducer cruiseUpdatedProducer;

    @Async
    public void findCruiseStopsAndTargetUsersAndSendCruiseStopUpdatedEvent(
            Cruise cruise, Stop updatedStop, LocalDateTime oldPlannedArrival, LocalDateTime oldPlannedDeparture
    ) {
        Stop from = stopService.findFirstStopByCruiseIdWithStation(cruise.getId())
                .orElseThrow(() -> new InternalServerException(
                                "Ошибка на сервере",
                                String.format("Ошибка: у рейса %s отсутствует первая остановка", cruise.getId()),
                                ErrorCode.INVALID_DATA_STATE
                        )
                );
        Stop to = stopService.findLastStopByCruiseIdWithStation(cruise.getId())
                .orElseThrow(() -> new InternalServerException(
                                "Ошибка на сервере",
                                String.format("Ошибка: у рейса %s отсутствует последняя остановка", cruise.getId()),
                                ErrorCode.INVALID_DATA_STATE
                        )
                );

        List<User> targetUsers = cruiseRepository.findUsersGoingFromOrToStopByCruiseIdAndStopId(
                cruise.getId(), updatedStop.getId()
        );

        CruiseUpdated event = CruiseUpdated.builder()
                .cruise(
                        ru.antonov.kafka.Cruise
                                .builder()
                                .id(cruise.getId())
                                .number(cruise.getNumber())
                                .startLocation(from.getStation().getLocation())
                                .startStation(from.getStation().getName())
                                .endLocation(to.getStation().getLocation())
                                .endStation(to.getStation().getName())
                                .status(cruise.getStatus().name())
                                .build()
                )
                .targetUsers(
                        targetUsers
                                .stream()
                                .map(u -> ru.antonov.kafka.User.builder()
                                        .id(u.getId())
                                        .name(u.getName())
                                        .surname(u.getSurname())
                                        .patronymic(u.getPatronymic())
                                        .email(u.getEmail())
                                        .build())
                                .toList()
                )
                .updatedStopStationLocation(updatedStop.getStation().getLocation())
                .updatedStopStationName(updatedStop.getStation().getName())
                .oldPlannedArrival(oldPlannedArrival)
                .oldPlannedDeparture(oldPlannedDeparture)
                .newPlannedArrival(updatedStop.getPlannedArrival())
                .newPlannedDeparture(updatedStop.getPlannedDeparture())
                .build();

        cruiseUpdatedProducer.sendCruiseUpdatedEvent(event);
    }
}
