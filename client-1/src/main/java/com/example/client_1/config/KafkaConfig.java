package com.example.client_1.config;

import com.example.common.config.KafkaPayloadConsumerConfig;
import com.example.common.config.KafkaPayloadListenerConfig;
import com.example.common.mapper.ProcedureMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@Import({KafkaPayloadConsumerConfig.class, KafkaPayloadListenerConfig.class, ProcedureMapper.class})
public class KafkaConfig {
}
