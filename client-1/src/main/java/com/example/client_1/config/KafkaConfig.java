package com.example.client_1.config;

import com.example.common.config.ConsumerKafkaConfig;
import com.example.common.config.ProducerKafkaConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ProducerKafkaConfig.class, ConsumerKafkaConfig.class})
public class KafkaConfig {
}
