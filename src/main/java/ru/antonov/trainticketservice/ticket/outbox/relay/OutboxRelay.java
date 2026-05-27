package ru.antonov.trainticketservice.ticket.outbox.relay;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.antonov.trainticketservice.ticket.eventstore.entity.Event;
import ru.antonov.trainticketservice.ticket.outbox.entity.OutboxEntry;
import ru.antonov.trainticketservice.ticket.outbox.repository.OutboxRepository;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Фоновый relay, публикующий ожидающие записи Transactional Outbox в Kafka.
 * <p>
 * Записи отправляются с несколькими повторными попытками, после чего
 * переносятся в dead-letter topic.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxRelay {
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 5;
    private static final long RETRY_DELAY_MS = 5000;
    private static final String DLQ_TOPIC = "topic.DLQ";

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper;

    private final OutboxRepository outboxRepository;

    /**
     * Публикует пакет ожидающих записей Outbox в Kafka topics.
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishPendingEvents() {
        List<OutboxEntry> entries = fetchPendingEntries();

        for (OutboxEntry entry : entries) {
            try {
                processEntry(entry);
            } catch (Exception e) {
                handleFailure(entry, e);
            }
        }
    }

    private List<OutboxEntry> fetchPendingEntries() {
        return outboxRepository.fetchPendingEntries((int) RETRY_DELAY_MS / 1000, BATCH_SIZE);
    }

    private void processEntry(OutboxEntry entry) throws Exception {
        outboxRepository.updateLastAttemptAndRetryCount(entry.getId(), LocalDateTime.now());

        String topic = mapEventTypeToTopic(entry.getEventType());

        kafkaTemplate.send(MessageBuilder
                .withPayload(entry.getPayload().toString())
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("entryId", entry.getId())
                .build()
        ).get(10, TimeUnit.SECONDS);

        outboxRepository.markProcessed(entry.getId(), LocalDateTime.now());
    }

    private void handleFailure(OutboxEntry entry, Exception e) {
        if (entry.getRetryCount() >= MAX_RETRY_COUNT - 1) {
            sendToDlq(entry, e);
            outboxRepository.markDead(entry.getId(), LocalDateTime.now());
        }
    }

    private void sendToDlq(OutboxEntry entry, Exception e) {
        try {
            Map<String, Object> dlqMessage = new HashMap<>();
            dlqMessage.put("payload", entry.getPayload());
            dlqMessage.put("entryId", entry.getId().toString());
            dlqMessage.put("retryCount", entry.getRetryCount());
            dlqMessage.put("lastAttemptAt", LocalDateTime.now().toString());
            dlqMessage.put("errorMessage", e.getMessage());
            dlqMessage.put("errorClass", e.getClass().getName());
            dlqMessage.put("sourceService", "train-ticket-service");
            dlqMessage.put("eventType", entry.getEventType());

            String dlqPayload = mapper.writeValueAsString(dlqMessage);

            kafkaTemplate.send(DLQ_TOPIC, entry.getId().toString(), dlqPayload)
                    .get(10, TimeUnit.SECONDS);

            log.warn("Outbox entry отправлен в DLQ: entryId={}, eventType={}",
                    entry.getId(), entry.getEventType());

        } catch (Exception dlqException) {
            log.error("FATAL: не удалось отправить в DLQ entryId={}", entry.getId(), dlqException);
        }
    }

    private String mapEventTypeToTopic(Event.EventType eventType) {
        return switch (eventType) {
            case TICKET_RESERVED -> "ticket-reserved-topic";
            case TICKET_BOOKED -> "ticket-booked-topic";
            case TICKET_FAILED_TO_BOOK -> "ticket-failed-to-book-topic";
            case TICKET_CANCELLED -> "ticket-cancelled-topic";
            case TICKET_CANCEL_PENDING -> "ticket-cancel-pending-topic";
            case TICKET_FAILED_TO_CANCEL -> "ticket-failed-to-cancel-topic";
        };
    }
}
