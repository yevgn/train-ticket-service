package ru.antonov.trainticketservice.ticket.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import ru.antonov.trainticketservice.ticket.command.command.TicketFailToBookCommand;
import ru.antonov.trainticketservice.ticket.command.handler.TicketFailedToBookHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class TicketPaymentFailedConsumer {

    private final ObjectMapper objectMapper;
    private final TicketFailedToBookHandler handler;

    @KafkaListener(
            topics = "ticket-payment-failed-topic",
            groupId = "command-ticket-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTicketPaymentFailedEvent(
            @Payload String payload,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition
    ) throws Exception {
        TicketFailToBookCommand command = objectMapper.readValue(payload, TicketFailToBookCommand.class);
        handler.handle(command);
    }
}

