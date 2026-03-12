package ru.antonov.train_ticket_service.ticket_purchase.message_producer.ticket_returned;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import ru.antonov.kafka.events.TicketReturned;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Cruise;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Ticket;
import ru.antonov.train_ticket_service.user.entity.User;

@Service
@Slf4j
public class TicketReturnedProducer {
    @Autowired
    @Qualifier("ticketReturnedKafkaTemplate")
    private KafkaTemplate<String, TicketReturned> kafkaTemplate;

    // макс число попыток отправки события в брокер - 5 (указано в application.yaml)
    public void sendTicketReturnedEvent(TicketReturned event) {
        kafkaTemplate.send("ticket-return-topic", event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Ошибка отправки события {}", event, ex);
                    } else {
                        log.info("Событие {} отправлено", event);
                    }
                });
    }
}