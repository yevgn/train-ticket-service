package ru.antonov.trainticketservice.ticket.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import ru.antonov.trainticketservice.ticket.command.command.TicketFailToCancelCommand;
import ru.antonov.trainticketservice.ticket.command.handler.TicketFailedToCancelHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class TicketRefundFailedConsumer {

    private final ObjectMapper objectMapper;
    private final TicketFailedToCancelHandler handler;

    @KafkaListener(
            topics = "ticket-refund-failed-topic",
            groupId = "command-ticket-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleRefundCompletedEvent(
            @Payload String payload,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition
    ) throws Exception {
        TicketFailToCancelCommand command = objectMapper.readValue(payload, TicketFailToCancelCommand.class);
        handler.handle(command);
    }
}