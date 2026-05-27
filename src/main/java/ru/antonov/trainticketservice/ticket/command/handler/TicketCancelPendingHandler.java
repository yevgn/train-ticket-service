package ru.antonov.trainticketservice.ticket.command.handler;


import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import ru.antonov.trainticketservice.common.exception.*;
import ru.antonov.trainticketservice.ticket.command.aggregate.TicketAggregate;
import ru.antonov.trainticketservice.ticket.command.command.Command;
import ru.antonov.trainticketservice.ticket.command.command.TicketCancelPendingCommand;
import ru.antonov.trainticketservice.ticket.command.repository.EventSourcedTicketRepositoryWithOutbox;

import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.EventDataMapper;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.TicketBookedEventData;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.TicketCancelPendingEventData;
import ru.antonov.trainticketservice.ticket.eventstore.repository.EventRepository;

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

/**
 * Обрабатывает пользовательский запрос на начало отмены билета.
 * <p>
 * Проверяет владельца, текущий статус и допустимое время отмены, затем
 * фиксирует событие ожидания отмены для асинхронной обработки возврата.
 */
@Component
@Slf4j
public class TicketCancelPendingHandler extends HandlerRoot<TicketAggregate>{
    private final int MAX_HOURS_BEFORE_TO_CANCEL = 24;

    public TicketCancelPendingHandler(
            EventRepository eventRepository,
            EventSourcedTicketRepositoryWithOutbox eventSourcedTicketRepositoryWithOutbox,
            EventDataMapper eventDataMapper
    ) {
        super(eventRepository, eventSourcedTicketRepositoryWithOutbox, eventDataMapper, TicketAggregate::new);
    }

    private void validateAggregateExists(TicketAggregate aggregate, UUID userId) {
        if (aggregate.getStatus() == null) {
            throw new EntityNotFoundException(
                    "Данного билета не существует",
                    String.format("Ошибка возврата билета пользователем %s. Билета %s не существует",
                            userId, aggregate.getId()),
                    ErrorCode.ENTITY_NOT_FOUND
            );
        }
    }

    private void validateOwner(TicketAggregate aggregate, UUID userId) {
        if (!userId.equals(aggregate.getOwnerId())) {
            throw new ResourceAccessDeniedException(
                    "Ошибка доступа. Данный билет принадлежит другому пользователю",
                    String.format("Ошибка возрата билета пользователем %s. Билет %s принадлежит другому" +
                            " пользователю: %s", userId, aggregate.getId(), aggregate.getOwnerId()),
                    ErrorCode.FORBIDDEN
            );
        }
    }

    private void validateStatus(TicketAggregate aggregate) {
        if (aggregate.getStatus() == TicketAggregate.Status.CANCELLED) {
            throw new TicketAlreadyCancelledException(
                    "Ошибка. Данный билет уже был отменен вами",
                    String.format("Ошибка возрата билета пользователем %s. Билет %s уже cancelled",
                            aggregate.getOwnerId(), aggregate.getId()),
                    ErrorCode.DATA_CONFLICT
            );
        } else if (aggregate.getStatus() == TicketAggregate.Status.CANCEL_PENDING) {
            throw new AcceptedException(
                    "Запрос уже находится в обработке",
                    String.format("AcceptedException при отмене билета %s пользователем %s. Запрос уже находится" +
                            " в обработке", aggregate.getId(), aggregate.getOwnerId()),
                    ErrorCode.ACCEPTED
            );
        } else if (aggregate.getStatus() != TicketAggregate.Status.BOOKED) {
            throw new TicketNotBookedException(
                    "Ошибка. Нельзя отменить незабронированный билет",
                    String.format("Ошибка возрата билета пользователем %s. Билет %s не BOOKED",
                            aggregate.getOwnerId(), aggregate.getId()),
                    ErrorCode.BAD_REQUEST
            );
        }
    }

    private void validateCancelTime(TicketAggregate aggregate, UUID userId, LocalDateTime departureTime) {
        if (!canCancel(departureTime)) {
            throw new TicketCancelTimeValidationException(
                    String.format("Ошибка. Билет можно вернуть максимум за %s часа до отправления",
                            MAX_HOURS_BEFORE_TO_CANCEL),
                    String.format("Ошибка при сдаче билета %s пользователем %s: меньше %s часов до отправления",
                            aggregate.getId(), userId, MAX_HOURS_BEFORE_TO_CANCEL),
                    ErrorCode.UNPROCESSABLE_ENTITY
            );
        }
    }

    private TicketBookedEventData extractDataFromBookEvent(List<Event> history) {
        Event last = history.get(history.size() - 1);

        return (TicketBookedEventData) eventDataMapper.toEventData(last);
    }

    /**
     * Добавляет событие ожидания отмены после проверки запроса.
     *
     * @param aggregate агрегат билета, восстановленный из истории
     * @param command команда запроса отмены
     */
    @Override
    public void applyEvent(TicketAggregate aggregate, Command command) {
        UUID userId = ((TicketCancelPendingCommand) command).getUserId();
        // валидация владельца билета + статуса
        validateAggregateExists(aggregate, userId );
        validateOwner(aggregate, userId);
        validateStatus(aggregate);

        // проверка на возможность вернуть билет (24 часа)
        validateCancelTime(aggregate, userId, aggregate.getDepartureTime());

        List<Event> history = aggregate.getHistory();
        TicketBookedEventData bookData = extractDataFromBookEvent(history);

        TicketCancelPendingEventData data = TicketCancelPendingEventData.builder()
                .aggregateId(aggregate.getId())
                .ownerId(aggregate.getOwnerId())
                .amount(bookData.getFare())
                .idempotencyKey(generateIdempotencyKey())
                .paymentId(bookData.getPaymentId())
                .build();

        aggregate.pendTicketToCancel(data);
    }

    private UUID generateIdempotencyKey(){
        return UUID.randomUUID();
    }

    private boolean canCancel(LocalDateTime departureTime) {
        return LocalDateTime.now()
                .plusHours(MAX_HOURS_BEFORE_TO_CANCEL)
                .isBefore(departureTime);
    }
}
