package com.example.common.config;

import com.example.common.model.ProcedureRequest;
import com.example.common.model.ProcedureResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaListenerConfig {

    // Внимание! Никогда не используем финалы или конструктор для внедрения зависимостей из других config-классов

    /**
     * Listener container factory для обработки запросов
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcedureRequest> kafkaListenerContainerFactory(
            @Qualifier("requestConsumerFactory") ConsumerFactory<String, ProcedureRequest> consumerFactory,
            @Qualifier("kafkaTemplate") KafkaTemplate<String, ProcedureResponse> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, ProcedureRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.setReplyTemplate(kafkaTemplate);
        return factory;
    }
}