package ru.antonov.trainticketservice.cruise.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ru.antonov.trainticketservice.common.exception.ErrorCode;
import ru.antonov.trainticketservice.common.exception.InternalServerException;
import ru.antonov.trainticketservice.cruise.entity.Cruise;
import ru.antonov.trainticketservice.cruise.entity.Stop;

import ru.antonov.trainticketservice.cruise.kafka.event.CruiseUpdatedEvent;

import ru.antonov.trainticketservice.ticket.query.dto.TicketShortDto;
import ru.antonov.trainticketservice.ticket.query.service.TicketQueryService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CruiseAsyncService {
    private final StopService stopService;
    private final TicketQueryService ticketQueryService;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

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

        List<TicketShortDto> targetTickets = ticketQueryService.findAllByCruiseIdAndStopId(
                cruise.getId(), updatedStop.getId()
        );

        CruiseUpdatedEvent event = CruiseUpdatedEvent.builder()
                .cruiseNumber(cruise.getNumber())
                .cruiseId(cruise.getId())
                .newArrival(updatedStop.getPlannedArrival())
                .newDeparture(updatedStop.getPlannedDeparture())
                .oldArrival(oldPlannedArrival)
                .oldDeparture(oldPlannedDeparture)
                .updatedStopId(updatedStop.getId())
                .updatedStopName(updatedStop.getStation().getName())
                .updatedStopLocation(updatedStop.getStation().getLocation())
                .targetUsers(
                        targetTickets.stream()
                                .map(ticket -> CruiseUpdatedEvent.User.builder()
                                        .id(ticket.getOwnerId())
                                        .email(ticket.getOwnerEmail())
                                        .fullName(ticket.getOwnerFullName())
                                        .build())
                                .toList()
                )
                .build();

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(
                    MessageBuilder
                            .withPayload(payload)
                            .setHeader(KafkaHeaders.TOPIC, "cruise-updated-topic")
                            .build()
            ).whenComplete((res, ex) -> {
                log.error("FATAL: сообщение {} не было доставлено в брокер", event, ex);
            });
        } catch (JsonProcessingException ex) {
            log.error("FATAL: неудача отправки CruiseUpdatedEvent {} в брокер: JsonProcessingException ", event, ex);
        }

    }
}
