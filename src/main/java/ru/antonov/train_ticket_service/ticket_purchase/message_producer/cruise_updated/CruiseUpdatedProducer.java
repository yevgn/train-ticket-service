package ru.antonov.train_ticket_service.ticket_purchase.message_producer.cruise_updated;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.antonov.kafka.events.CruiseUpdated;

@Service
@Slf4j
public class CruiseUpdatedProducer {
    @Autowired
    @Qualifier("cruiseUpdatedKafkaTemplate")
    private KafkaTemplate<String, CruiseUpdated> kafkaTemplate;

    public void sendCruiseUpdatedEvent(CruiseUpdated event) {
        kafkaTemplate.send("cruise-update-topic", event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Ошибка отправки события {}", event, ex);
                    } else {
                        log.info("Событие {} отправлено", event);
                    }
                });
    }
}
