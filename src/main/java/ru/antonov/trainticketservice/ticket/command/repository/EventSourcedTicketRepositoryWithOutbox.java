package ru.antonov.trainticketservice.ticket.command.repository;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.antonov.trainticketservice.ticket.command.aggregate.AggregateRoot;
import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;
import ru.antonov.trainticketservice.ticket.eventstore.repository.EventRepository;
import ru.antonov.trainticketservice.ticket.outbox.entity.OutboxEntry;
import ru.antonov.trainticketservice.ticket.outbox.repository.OutboxRepository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventSourcedTicketRepositoryWithOutbox {
    private final OutboxRepository outboxRepository;
    private final EventRepository eventRepository;

    private final ObjectMapper mapper;

    @Transactional
    public void save(AggregateRoot aggregate) throws JsonProcessingException {
        List<Event> uncommittedEvents = aggregate.getUncommitedEvents();
        if (uncommittedEvents.isEmpty()) {
            return;
        }

        for (Event event : uncommittedEvents) {
            event.setPayload(mapper.writeValueAsString(event.getEventData()));
            saveToEventStore(event);

            OutboxEntry entry = OutboxEntry.builder()
                    .aggregateId(aggregate.getId())
                    .aggregateType("Ticket")
                    .eventType(event.getEventType())
                    .payload(event.getPayload())
                    .status(OutboxEntry.Status.PENDING)
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            saveOutboxEntry(entry);
        }

        aggregate.markEventsAsCommitted();
    }

    private void saveOutboxEntry(OutboxEntry entry) {
        outboxRepository.save(entry);
    }

    private void saveToEventStore(Event event) {
        eventRepository.save(event);
    }
}
