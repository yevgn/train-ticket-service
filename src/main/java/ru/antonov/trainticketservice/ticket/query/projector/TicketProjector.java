package ru.antonov.trainticketservice.ticket.query.projector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import java.util.UUID;

/**
 * Kafka consumer, направляющий события билетов из command-side topics
 * в сервис построения query-side проекций.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketProjector {
    private final TicketProjectorService projectorService;

    /**
     * Извлекает идентификатор записи Outbox из Kafka headers и делегирует построение проекции.
     *
     * @param record Kafka record с topic, payload и header entryId
     */
    @KafkaListener(
            topics = {"ticket-reserved-topic", "ticket-booked-topic", "ticket-failed-to-book-topic",
                    "ticket-cancel-pending-topic", "ticket-cancelled-topic", "ticket-failed-to-cancel-topic"},
            groupId = "query-ticket-service"
    )
    public void handle(ConsumerRecord<String, String> record){
        UUID entryId = UUID.fromString(
                new String(
                        record.headers().lastHeader("entryId").value(),
                        StandardCharsets.UTF_8
                )
        );

        projectorService.handle(entryId, record.topic(), record.value());
    }
}
