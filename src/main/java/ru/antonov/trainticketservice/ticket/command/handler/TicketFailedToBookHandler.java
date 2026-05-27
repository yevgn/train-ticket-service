package ru.antonov.trainticketservice.ticket.command.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.antonov.trainticketservice.cruise.entity.Cruise;
import ru.antonov.trainticketservice.cruise.entity.Stop;

import ru.antonov.trainticketservice.seat.entity.Seat;

import ru.antonov.trainticketservice.ticket.command.aggregate.TicketAggregate;

import ru.antonov.trainticketservice.ticket.command.command.Command;
import ru.antonov.trainticketservice.ticket.command.command.TicketFailToBookCommand;
import ru.antonov.trainticketservice.ticket.command.repository.EventSourcedTicketRepositoryWithOutbox;
import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.EventDataMapper;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.TicketFailedToBookEventData;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.TicketReservedEventData;
import ru.antonov.trainticketservice.ticket.eventstore.repository.EventRepository;
import ru.antonov.trainticketservice.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class TicketFailedToBookHandler extends HandlerRoot<TicketAggregate> {

    public TicketFailedToBookHandler(
            EventRepository eventRepository,
            EventSourcedTicketRepositoryWithOutbox eventSourcedTicketRepositoryWithOutbox,
            EventDataMapper eventDataMapper
    ) {
        super(eventRepository, eventSourcedTicketRepositoryWithOutbox, eventDataMapper, TicketAggregate::new);
    }

    private void validateStatus(TicketAggregate aggregate) {
        if (aggregate.getStatus() != TicketAggregate.Status.RESERVED) {
            log.error("FATAL: ошибка FAIL билета {}. Ожидался статус RESERVED, фактически: {}",
                    aggregate, aggregate.getStatus());
        }
    }

    private TicketReservedEventData extractDataFromReserveEvent(List<Event> history) {
        Event last = history.get(history.size() - 1);

        return (TicketReservedEventData) eventDataMapper.toEventData(last);
    }

    @Override
    public void applyEvent(TicketAggregate aggregate, Command command) {
        if (aggregate.getStatus() == TicketAggregate.Status.CANCELLED) return;

        validateStatus(aggregate);

        List<Event> history = aggregate.getHistory();
        TicketReservedEventData reserveData = extractDataFromReserveEvent(history);

        TicketFailedToBookEventData failData = TicketFailedToBookEventData.builder()
                .failedAt(LocalDateTime.now())
                .aggregateId(aggregate.getId())
                .seatId(reserveData.getSeatId())
                .seatNumber(reserveData.getSeatNumber())
                .cruiseId(reserveData.getCruiseId())
                .cruiseNumber(reserveData.getCruiseNumber())
                .fromStopId(reserveData.getFromStopId())
                .fromLocation(reserveData.getFromLocation())
                .fromStation(reserveData.getFromStation())
                .toStopId(reserveData.getToStopId())
                .toLocation(reserveData.getToLocation())
                .toStation(reserveData.getToStation())
                .ownerId(reserveData.getOwnerId())
                .ownerEmail(reserveData.getOwnerEmail())
                .ownerFullName(reserveData.getOwnerSurname() + " " + reserveData.getOwnerName() +
                        " " + reserveData.getOwnerPatronymic())
                .plannedDeparture(reserveData.getDepartureTime())
                .errorMessage(((TicketFailToBookCommand) command).getErrorMessage())
                .paymentId(((TicketFailToBookCommand) command).getPaymentId())
                .build();

        aggregate.failToBookTicket(failData);
    }

}
