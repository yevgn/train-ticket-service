package ru.antonov.trainticketservice.ticket.command.aggregate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event-sourced агрегат, описывающий жизненный цикл билета.
 * <p>
 * Все изменения состояния фиксируются как доменные события, которые затем
 * сохраняются event-sourced репозиторием.
 */
@SuperBuilder
@Getter
public class TicketAggregate extends AggregateRoot {
    private Status status;
    private UUID ownerId;
    private LocalDateTime departureTime;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Создает агрегат билета для указанного идентификатора.
     *
     * @param aggregateId идентификатор агрегата билета
     */
    public TicketAggregate(UUID aggregateId){
        super(aggregateId);
    }

    /**
     * Применяет событие билета к текущему состоянию агрегата в памяти.
     *
     * @param event метаданные события, содержащие тип события и версию
     * @param data payload
     */
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

    /**
     * Фиксирует событие резервирования билета.
     *
     * @param data данные резервирования
     */
    public void reserveTicket(TicketReservedEventData data) {
        createEventAndApply(data, Event.EventType.TICKET_RESERVED);
    }

    /**
     * Фиксирует запрос на отмену билета.
     *
     * @param data данные запроса на отмену
     */
    public void pendTicketToCancel(TicketCancelPendingEventData data) {
        createEventAndApply(data, Event.EventType.TICKET_CANCEL_PENDING);
    }

    /**
     * Фиксирует неудачное бронирование или ошибку оплаты.
     *
     * @param data данные ошибки бронирования
     */
    public void failToBookTicket(TicketFailedToBookEventData data) {
        createEventAndApply(data, Event.EventType.TICKET_FAILED_TO_BOOK);
    }

    /**
     * Фиксирует неудачную отмену или ошибку возврата средств.
     *
     * @param data данные ошибки отмены
     */
    public void failToCancelTicket(TicketFailedToCancelEventData data){
        createEventAndApply(data, Event.EventType.TICKET_FAILED_TO_CANCEL);
    }

    /**
     * Фиксирует успешное бронирование билета.
     *
     * @param data данные подтверждения бронирования
     */
    public void bookTicket(TicketBookedEventData data) {
        createEventAndApply(data, Event.EventType.TICKET_BOOKED);
    }

    /**
     * Фиксирует успешную отмену билета.
     *
     * @param data данные подтверждения отмены
     */
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
