package com.example.booking_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic bookingEventsTopic() {
        return new NewTopic("booking-events", 1, (short) 1);
    }
}