package com.example.common.config;

import com.example.common.model.ProcedurePayload;
import com.example.common.model.ProcedureResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaPayloadListenerConfig {

    /**
     * Listener container factory для обработки запросов
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcedurePayload<?>> kafkaListenerContainerFactory(
            @Qualifier("requestConsumerFactory") ConsumerFactory<String, ProcedurePayload<?>> consumerFactory,
            @Qualifier("kafkaTemplate") KafkaTemplate<String, ProcedureResponse<?>> kafkaTemplate
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ProcedurePayload<?>>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.setReplyTemplate(kafkaTemplate);
        return factory;
    }
}