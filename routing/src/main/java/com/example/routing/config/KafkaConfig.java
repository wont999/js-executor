package com.example.routing.config;

import com.example.common.config.KafkaReplyingTemplateConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@Import(KafkaReplyingTemplateConfig.class)
public class KafkaConfig {
}
