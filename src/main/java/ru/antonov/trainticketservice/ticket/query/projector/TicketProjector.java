package ru.antonov.trainticketservice.ticket.query.projector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketProjector {
    private final TicketProjectorService projectorService;

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