package ru.antonov.trainticketservice.ticket.command.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import ru.antonov.trainticketservice.common.exception.*;
import ru.antonov.trainticketservice.ticket.command.aggregate.AggregateRoot;

import ru.antonov.trainticketservice.ticket.command.command.Command;
import ru.antonov.trainticketservice.ticket.command.repository.EventSourcedTicketRepositoryWithOutbox;
import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;
import ru.antonov.trainticketservice.ticket.eventstore.eventdata.EventDataMapper;
import ru.antonov.trainticketservice.ticket.eventstore.repository.EventRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;


@RequiredArgsConstructor
@Slf4j
public abstract class HandlerRoot<T extends AggregateRoot> {
    private final EventRepository eventRepository;
    private final EventSourcedTicketRepositoryWithOutbox eventSourcedTicketRepositoryWithOutbox;

    protected final EventDataMapper eventDataMapper;

    private final Function<UUID, T> aggregateFactory;

    @Retryable(
            retryFor = {DataIntegrityViolationException.class, OptimisticLockException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void handle(Command command) {
        T aggregate = aggregateFactory.apply(command.getAggregateId());

        List<Event> history = eventRepository.load(command.getAggregateId());

        aggregate.loadFromHistory(history, eventDataMapper);

        applyEvent(aggregate, command);

        try {
            eventSourcedTicketRepositoryWithOutbox.save(aggregate);
        } catch (JsonProcessingException ex){
            throw new InternalServerException(
                    "Ошибка на сервере",
                    String.format("Неудача выполнения command %s из-за JsonProcessingException: %s. Агрегат %s",
                            command, ex, aggregate),
                    ErrorCode.SERVER_ERROR
            );

        } catch (DataIntegrityViolationException ex) {
            Throwable root = NestedExceptionUtils.getRootCause(ex);

            if (root instanceof SQLException sqlEx && sqlEx.getMessage().toLowerCase().contains("unique") &&
                    sqlEx.getMessage().toLowerCase().contains("aggregate_id") &&
                    sqlEx.getMessage().toLowerCase().contains("aggregate_version")) {
                throw new OptimisticLockException();
            } else {
                throw ex;
            }
        }
    }

    @Recover
    public void recover(OptimisticLockException e, Command command) {
        log.error("Неудача сохранения события {} после 3 попыток :", command.getAggregateId(), e);
        throw new ConcurrentAggregateModificationException(
                "Попробуйте повторить запрос",
                String.format("ConcurrentAggregateModificationException : %s. %s", e, command.getAggregateId()),
                ErrorCode.DATA_CONFLICT
        );
    }

    @Recover
    public void recover(DataIntegrityViolationException e, Command command) {
        log.error("Неудача сохранения события {} после 3 попыток :", command.getAggregateId(), e);
        throw new DatabaseException(
                "Попробуйте повторить запрос",
                String.format("DataIntegrityViolationException : %s. %s", e, command.getAggregateId()),
                ErrorCode.DATA_CONFLICT
        );
    }

    public abstract void applyEvent(T aggregate, Command command);
}
