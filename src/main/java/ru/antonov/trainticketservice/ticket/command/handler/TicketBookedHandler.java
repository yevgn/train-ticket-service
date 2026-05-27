package ru.antonov.trainticketservice.ticket.command.handler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import ru.antonov.trainticketservice.ticket.command.aggregate.TicketAggregate;
import ru.antonov.trainticketservice.ticket.command.command.Command;
import ru.antonov.trainticketservice.ticket.command.command.TicketBookCommand;

import ru.antonov.trainticketservice.ticket.command.repository.EventSourcedTicketRepositoryWithOutbox;
import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.EventDataMapper;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.TicketBookedEventData;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.TicketReservedEventData;
import ru.antonov.trainticketservice.ticket.eventstore.repository.EventRepository;


import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class TicketBookedHandler extends HandlerRoot<TicketAggregate> {
    public TicketBookedHandler(
            EventRepository eventRepository,
            EventSourcedTicketRepositoryWithOutbox eventSourcedTicketRepositoryWithOutbox,
            EventDataMapper eventDataMapper
    ) {
        super(eventRepository, eventSourcedTicketRepositoryWithOutbox, eventDataMapper, TicketAggregate::new);
    }

    @Override
    public void applyEvent(TicketAggregate aggregate, Command command) {
        if (aggregate.getStatus() == TicketAggregate.Status.BOOKED) return;

        validateStatus(aggregate);

        List<Event> history = aggregate.getHistory();
        TicketReservedEventData reserveData = extractDataFromReserveEvent(history);

        TicketBookedEventData bookData = TicketBookedEventData.builder()
                .bookedAt(LocalDateTime.now())
                .aggregateId(aggregate.getId())
                .ownerId(reserveData.getOwnerId())
                .ownerEmail(reserveData.getOwnerEmail())
                .ownerFullName(
                        reserveData.getOwnerSurname() + " " + reserveData.getOwnerName() +
                                " " + reserveData.getOwnerPatronymic())
                .seatNumber(reserveData.getSeatNumber())
                .cruiseNumber(reserveData.getCruiseNumber())
                .fromLocation(reserveData.getFromLocation())
                .toLocation(reserveData.getToLocation())
                .fromStation(reserveData.getFromStation())
                .toStation(reserveData.getToStation())
                .plannedDeparture(reserveData.getDepartureTime())
                .fare(reserveData.getFare())
                .bookedAt(LocalDateTime.now())
                .paymentId(((TicketBookCommand) command).getPaymentId())
                .fromStopId(reserveData.getFromStopId())
                .toStopId(reserveData.getToStopId())
                .seatId(reserveData.getSeatId())
                .cruiseId(reserveData.getCruiseId())
                .build();

        aggregate.bookTicket(bookData);
    }

    private void validateStatus(TicketAggregate aggregate) {
        if (aggregate.getStatus() != TicketAggregate.Status.RESERVED) {
            log.error("FATAL: ошибка бронирования билета {}. Ожидался статус RESERVED, фактически: {}",
                    aggregate, aggregate.getStatus());
        }
    }

    private TicketReservedEventData extractDataFromReserveEvent(List<Event> history) {
        Event last = history.get(history.size() - 1);

        return (TicketReservedEventData) eventDataMapper.toEventData(last);
    }

}
