package com.example.common.config;

import com.example.common.model.ProcedureResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
public class KafkaResponseListenerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcedureResponse<?>> responseKafkaListenerContainerFactory(
            @Qualifier("responseConsumerFactory") ConsumerFactory<String, ProcedureResponse<?>> responseConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ProcedureResponse<?>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(responseConsumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }
}