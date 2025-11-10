package com.example.client_1.config;

import com.example.common.config.KafkaConsumerConfig;
import com.example.common.config.KafkaListenerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@Import({
    KafkaConsumerConfig.class,
    KafkaListenerConfig.class
})
public class KafkaConfig {
}
