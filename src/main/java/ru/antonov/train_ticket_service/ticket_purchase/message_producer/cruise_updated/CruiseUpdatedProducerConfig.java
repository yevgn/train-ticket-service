package ru.antonov.train_ticket_service.ticket_purchase.message_producer.cruise_updated;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.antonov.kafka.events.CruiseUpdated;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CruiseUpdatedProducerConfig {

    @Bean
    public ProducerFactory<String, CruiseUpdated> cruiseUpdatedProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, CruiseUpdated> cruiseUpdatedKafkaTemplate() {
        return new KafkaTemplate<>(cruiseUpdatedProducerFactory());
    }
}
