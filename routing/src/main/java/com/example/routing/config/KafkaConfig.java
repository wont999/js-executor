package com.example.routing.config;

import com.example.common.config.KafkaResponseConsumerConfig;
import com.example.common.config.KafkaResponseListenerConfig;
import com.example.common.mapper.ProcedureMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@Import({KafkaResponseConsumerConfig.class, KafkaResponseListenerConfig.class, ProcedureMapper.class})
public class KafkaConfig {

    @Value("${gateway.instance-id}")
    String instanceId;

    @Bean
    public String instanceId() {
        return instanceId;
    }
}