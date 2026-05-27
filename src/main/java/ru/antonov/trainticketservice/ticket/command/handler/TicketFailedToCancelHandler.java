package ru.antonov.trainticketservice.ticket.command.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.antonov.trainticketservice.cruise.entity.Cruise;
import ru.antonov.trainticketservice.cruise.entity.Stop;
import ru.antonov.trainticketservice.cruise.service.CruiseService;
import ru.antonov.trainticketservice.cruise.service.StopService;
import ru.antonov.trainticketservice.seat.entity.Seat;
import ru.antonov.trainticketservice.seat.serivce.SeatService;
import ru.antonov.trainticketservice.ticket.command.aggregate.TicketAggregate;

import ru.antonov.trainticketservice.ticket.command.command.Command;
import ru.antonov.trainticketservice.ticket.command.command.TicketFailToCancelCommand;
import ru.antonov.trainticketservice.ticket.command.repository.EventSourcedTicketRepositoryWithOutbox;
import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.EventDataMapper;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.TicketCancelPendingEventData;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.TicketFailedToCancelEventData;
import ru.antonov.trainticketservice.ticket.eventstore.repository.EventRepository;
import ru.antonov.trainticketservice.user.entity.User;
import ru.antonov.trainticketservice.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;


@Component
@Slf4j
public class TicketFailedToCancelHandler extends HandlerRoot<TicketAggregate>{
    private final UserService userService;
    private final SeatService seatService;
    private final StopService stopService;
    private final CruiseService cruiseService;

    public TicketFailedToCancelHandler(
            EventRepository eventRepository,
            EventSourcedTicketRepositoryWithOutbox eventSourcedTicketRepositoryWithOutbox,
            EventDataMapper eventDataMapper,
            UserService userService,
            SeatService seatService,
            StopService stopService,
            CruiseService cruiseService
    ) {
        super(eventRepository, eventSourcedTicketRepositoryWithOutbox, eventDataMapper, TicketAggregate::new);
        this.userService = userService;
        this.seatService = seatService;
        this.stopService = stopService;
        this.cruiseService = cruiseService;
    }

    private TicketCancelPendingEventData extractDataFromCancelPendingEvent(List<Event> history) {
        Event last = history.get(history.size() - 1);

        return (TicketCancelPendingEventData) eventDataMapper.toEventData(last);
    }

    private void validateStatus(TicketAggregate aggregate){
        if(aggregate.getStatus() != TicketAggregate.Status.CANCEL_PENDING){
            log.error("FATAL: ошибка возврата билета {}. Ожидаемый статус CANCEL_PENDING. Фактический {}",
                    aggregate, aggregate.getStatus());
        }
    }

    @Override
    public void applyEvent(TicketAggregate aggregate, Command command) {
        if(aggregate.getStatus() == TicketAggregate.Status.BOOKED) return;

        validateStatus(aggregate);

        List<Event> history = aggregate.getHistory();
        TicketCancelPendingEventData cancelPendingData = extractDataFromCancelPendingEvent(history);

        User owner = userService.findById(aggregate.getOwnerId()).orElseThrow();
        Seat seat = seatService.findById(cancelPendingData.getSeatId()).orElseThrow();
        Stop from = stopService.findById(cancelPendingData.getStopFromId()).orElseThrow();
        Stop to = stopService.findById(cancelPendingData.getStopToId()).orElseThrow();
        Cruise cruise = cruiseService.findById(cancelPendingData.getCruiseId()).orElseThrow();

        TicketFailedToCancelEventData failedToCancelData = TicketFailedToCancelEventData.builder()
                .aggregateId(aggregate.getId())
                .failedAt(LocalDateTime.now())
                .ownerId(owner.getId())
                .ownerEmail(owner.getEmail())
                .ownerFullName(owner.getSurname() + " " + owner.getName() + " " + owner.getPatronymic())
                .cruiseId(cruise.getId())
                .cruiseNumber(cruise.getNumber())
                .stopFromId(from.getId())
                .fromStation(from.getStation().getName())
                .fromLocation(from.getStation().getLocation())
                .stopToId(to.getId())
                .toStation(to.getStation().getName())
                .toLocation(to.getStation().getLocation())
                .plannedDeparture(from.getPlannedDeparture())
                .seatId(seat.getId())
                .seatNumber(seat.getNumber())
                .refundId( ((TicketFailToCancelCommand)command).getRefundId())
                .errorMessage( ((TicketFailToCancelCommand)command).getErrorMessage())
                .amount(cancelPendingData.getAmount())
                .build();

        aggregate.failToCancelTicket(failedToCancelData);
    }


}