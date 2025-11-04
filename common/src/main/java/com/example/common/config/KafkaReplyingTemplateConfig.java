package com.example.common.config;

import com.example.common.model.ProcedureRequest;
import com.example.common.model.ProcedureResponse;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaReplyingTemplateConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    String bootstrapServers;

    @Value("${gateway.instance-id}")
    String instanceId;

    final ProducerFactory<String, ProcedureRequest> producerFactory;

    /**
     * Consumer для получения ответов (для request-reply паттерна)
     */
    @Bean
    public ConsumerFactory<String, ProcedureResponse> responseConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProcedureResponse.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Listener container factory для получения ответов
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcedureResponse> responseKafkaListenerContainerFactory(
            ConsumerFactory<String, ProcedureResponse> responseConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ProcedureResponse> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(responseConsumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    /**
     * Контейнер для получения ответов
     */
    @Bean
    public ConcurrentMessageListenerContainer<String, ProcedureResponse> repliesContainer(
            ConsumerFactory<String, ProcedureResponse> responseConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ProcedureResponse> containerFactory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(responseConsumerFactory);
        containerFactory.getContainerProperties().setGroupId(instanceId);
        
        return containerFactory.createContainer(instanceId);
    }

    /**
     * ReplyingKafkaTemplate для request-reply паттерна
     */
    @Bean
    public ReplyingKafkaTemplate<String, ProcedureRequest, ProcedureResponse> replyingKafkaTemplate(
            ConcurrentMessageListenerContainer<String, ProcedureResponse> repliesContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory, repliesContainer);
    }
}
