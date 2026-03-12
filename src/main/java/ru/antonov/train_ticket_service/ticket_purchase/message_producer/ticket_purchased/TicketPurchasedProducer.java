package ru.antonov.train_ticket_service.ticket_purchase.message_producer.ticket_purchased;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.antonov.kafka.events.TicketPurchased;
import ru.antonov.train_ticket_service.ticket_purchase.entity.Cruise;

import ru.antonov.train_ticket_service.ticket_purchase.entity.Ticket;
import ru.antonov.train_ticket_service.user.entity.User;

@Service
@Slf4j
public class TicketPurchasedProducer {
    @Autowired
    @Qualifier("ticketPurchasedKafkaTemplate")
    private KafkaTemplate<String, TicketPurchased> kafkaTemplate;

    public void sendTicketPurchasedEvent(TicketPurchased event) {
        kafkaTemplate.send("ticket-purchase-topic", event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Ошибка отправки события {}", event, ex);
                    } else {
                        log.info("Событие {} отправлено", event);
                    }
                });
    }

}
