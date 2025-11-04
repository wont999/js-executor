package com.example.common.config;

import com.example.common.model.ProcedureRequest;
import com.example.common.model.ProcedureResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaListenerConfig {

    final ConsumerFactory<String, ProcedureRequest> requestConsumerFactory;

    final KafkaTemplate<String, ProcedureResponse> kafkaTemplate;

    /**
     * Listener container factory для обработки запросов
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcedureRequest> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProcedureRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(requestConsumerFactory);
        factory.setConcurrency(3); // 3 потока для параллельной обработки
        factory.setReplyTemplate(kafkaTemplate); // Устанавливаем template для @SendTo
        return factory;
    }
}
