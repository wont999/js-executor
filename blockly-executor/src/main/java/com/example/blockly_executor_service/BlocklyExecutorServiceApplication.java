package com.example.blockly_executor_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

@Slf4j
@SpringBootApplication(scanBasePackages = {
    "com.example.blockly_executor_service",
    "com.example.common"  // Подключаем обработчики исключений из common
})
@EnableDiscoveryClient
@EnableKafka
public class BlocklyExecutorServiceApplication {

	public static void main(String[] args) {
		log.info("Starting BlocklyExecutorServiceApplication");
		SpringApplication.run(BlocklyExecutorServiceApplication.class, args);
		log.info("BlocklyExecutorServiceApplication started");
	}

}
