package com.cff.kafka.producer.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(KafkaProducerProperties.class)
public class KafkaProducerAutoConfiguration {
}
