package com.example.booking_service.infrastructure.messaging;

import com.example.booking_service.domain.events.BookingConfirmedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingEventPublisher {

    private final KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate;

    public BookingEventPublisher(KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        kafkaTemplate.send("booking-events", event);
        System.out.println("Sent " + event.getBookingId());
    }
}
