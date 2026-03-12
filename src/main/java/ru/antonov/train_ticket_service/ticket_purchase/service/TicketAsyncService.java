package ru.antonov.train_ticket_service.ticket_purchase.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.antonov.kafka.events.TicketPurchased;
import ru.antonov.kafka.events.TicketReturned;
import ru.antonov.train_ticket_service.common.exception.ErrorCode;
import ru.antonov.train_ticket_service.common.exception.InternalServerException;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Cruise;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Stop;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Ticket;

import ru.antonov.train_ticket_service.ticket_purchase.message_producer.ticket_purchased.TicketPurchasedProducer;
import ru.antonov.train_ticket_service.ticket_purchase.message_producer.ticket_returned.TicketReturnedProducer;
import ru.antonov.train_ticket_service.user.entity.User;

@Service
@RequiredArgsConstructor
public class TicketAsyncService {
    private final TicketReturnedProducer ticketReturnedProducer;
    private final TicketPurchasedProducer ticketPurchasedProducer;

    private final StopService stopService;

    @Async
    public void findCruiseStopsAndSendTicketReturnedEvent(Ticket ticket) {
        Cruise cruise = ticket.getCruise();
        User user = ticket.getUser();

        Stop cruiseStartStop = stopService.findFirstStopByCruiseIdWithStation(cruise.getId())
                .orElseThrow(() -> new InternalServerException(
                                "Ошибка на сервере",
                                String.format("Ошибка: у рейса %s отсутствует первая остановка", cruise.getId()),
                                ErrorCode.INVALID_DATA_STATE
                        )
                );
        Stop cruiseEndStop = stopService.findLastStopByCruiseIdWithStation(cruise.getId())
                .orElseThrow(() -> new InternalServerException(
                                "Ошибка на сервере",
                                String.format("Ошибка: у рейса %s отсутствует последняя остановка", cruise.getId()),
                                ErrorCode.INVALID_DATA_STATE
                        )
                );

        TicketReturned event = TicketReturned.builder()
                .cruise(
                        ru.antonov.kafka.Cruise
                                .builder()
                                .id(cruise.getId())
                                .number(cruise.getNumber())
                                .startLocation(cruiseStartStop.getStation().getLocation())
                                .startStation(cruiseStartStop.getStation().getName())
                                .endLocation(cruiseEndStop.getStation().getLocation())
                                .endStation(cruiseEndStop.getStation().getName())
                                .status(cruise.getStatus().name())
                                .build()
                )
                .user(
                        ru.antonov.kafka.User.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .surname(user.getSurname())
                                .patronymic(user.getPatronymic())
                                .email(user.getEmail())
                                .build()
                )
                .ticketId(ticket.getId())
                .fare(ticket.getActualFare())
                .departureStationName(ticket.getFrom().getStation().getName())
                .departureStationLocation(ticket.getFrom().getStation().getLocation())
                .arrivalStationLocation(ticket.getTo().getStation().getLocation())
                .arrivalStationName(ticket.getTo().getStation().getName())
                .departureTime(ticket.getFrom().getPlannedDeparture())
                .arrivalTime(ticket.getTo().getPlannedArrival())
                .purchasedAt(ticket.getPurchasedAt())
                .returnedAt(ticket.getReturnedAt())
                .build();

        ticketReturnedProducer.sendTicketReturnedEvent(event);
    }

    @Async
    public void sendTicketPurchasedEvent(
            Ticket ticket, Stop cruiseStartStop, Stop cruiseEndStop, User principal
    ) {

        TicketPurchased event = TicketPurchased.builder()
                .cruise(
                        ru.antonov.kafka.Cruise
                                .builder()
                                .id(ticket.getCruise().getId())
                                .number(ticket.getCruise().getNumber())
                                .startLocation(cruiseStartStop.getStation().getLocation())
                                .startStation(cruiseStartStop.getStation().getName())
                                .endLocation(cruiseEndStop.getStation().getLocation())
                                .endStation(cruiseEndStop.getStation().getName())
                                .status(ticket.getCruise().getStatus().name())
                                .build()
                )
                .user(
                        ru.antonov.kafka.User.builder()
                                .id(principal.getId())
                                .name(principal.getName())
                                .surname(principal.getSurname())
                                .patronymic(principal.getPatronymic())
                                .email(principal.getEmail())
                                .build()
                )
                .ticketId(ticket.getId())
                .fare(ticket.getActualFare())
                .departureStationLocation(ticket.getFrom().getStation().getLocation())
                .departureStationName(ticket.getFrom().getStation().getName())
                .arrivalStationLocation(ticket.getTo().getStation().getLocation())
                .arrivalStationName(ticket.getTo().getStation().getName())
                .purchasedAt(ticket.getPurchasedAt())
                .build();

        ticketPurchasedProducer.sendTicketPurchasedEvent(event);
    }
}
