package com.example.client_1.config;

import com.example.common.config.KafkaConsumerConfig;
import com.example.common.config.KafkaListenerConfig;
import com.example.common.config.KafkaProducerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    KafkaConsumerConfig.class,
    KafkaProducerConfig.class,
    KafkaListenerConfig.class
})
public class KafkaConfig {
}
