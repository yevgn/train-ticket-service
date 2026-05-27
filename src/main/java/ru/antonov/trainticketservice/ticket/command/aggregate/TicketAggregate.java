package ru.antonov.trainticketservice.ticket.command.aggregate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.*;

import java.time.LocalDateTime;
import java.util.UUID;

@SuperBuilder
@Getter
public class TicketAggregate extends AggregateRoot {
    private Status status;
    private UUID ownerId;
    private LocalDateTime departureTime;

    @Autowired
    private ObjectMapper mapper;

    public TicketAggregate(UUID aggregateId){
        super(aggregateId);
    }

    @Override
    public void handleEvent(Event event, EventData data) {

        switch (event.getEventType()) {
            case TICKET_RESERVED -> {
                TicketReservedEventData d = (TicketReservedEventData) data;

                this.status = Status.RESERVED;
                this.ownerId = d.getOwnerId();
                this.departureTime = d.getDepartureTime();
            }
            case TICKET_BOOKED -> this.status = Status.BOOKED;
            case TICKET_CANCEL_PENDING -> this.status = Status.CANCEL_PENDING;
            case TICKET_CANCELLED -> this.status = Status.CANCELLED;
            case TICKET_FAILED_TO_BOOK -> this.status = Status.CANCELLED;
            case TICKET_FAILED_TO_CANCEL -> this.status = Status.BOOKED;
        }
    }

    public void reserveTicket(TicketReservedEventData data) {
        createEventAndApply(data, Event.EventType.TICKET_RESERVED);
    }

    public void pendTicketToCancel(TicketCancelPendingEventData data) {
        createEventAndApply(data, Event.EventType.TICKET_CANCEL_PENDING);
    }

    public void failToBookTicket(TicketFailedToBookEventData data) {
        createEventAndApply(data, Event.EventType.TICKET_FAILED_TO_BOOK);
    }

    public void failToCancelTicket(TicketFailedToCancelEventData data){
        createEventAndApply(data, Event.EventType.TICKET_FAILED_TO_CANCEL);
    }

    public void bookTicket(TicketBookedEventData data) {
        createEventAndApply(data, Event.EventType.TICKET_BOOKED);
    }

    public void cancelTicket(TicketCancelledEventData data) {
        createEventAndApply(data, Event.EventType.TICKET_CANCELLED);
    }

    private void createEventAndApply(EventData data, Event.EventType eventType) {
        Event event = Event.builder()
                .eventType(eventType)
                .aggregateId(getId())
                .eventData(data)
                .aggregateType("Ticket")
                .timestamp(LocalDateTime.now())
                .build();

        applyEvent(event, data);
    }

    @Override
    public String toString() {
        return "TicketAggregate{" +
                "id=" + getId() +
                ",version=" + getVersion() +
                "status=" + status +
                ", ownerId=" + ownerId +
                ", departureTime=" + departureTime +
                '}';
    }

    public enum Status {
        CANCELLED,
        BOOKED,
        RESERVED,
        CANCEL_PENDING
    }
}
