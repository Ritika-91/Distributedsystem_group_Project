package com.example.notification.infrastructure.messaging;

import com.example.notification.application.NotificationApplicationService;
import com.example.notification.events.BookingConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventsListener {

    private static final Logger log = LoggerFactory.getLogger(BookingEventsListener.class);

    private final NotificationApplicationService notificationService;

    public BookingEventsListener(NotificationApplicationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "booking-events", groupId = "notification-service-group")
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        log.info("✅ Received BookingConfirmedEvent from Kafka: {}", event);
        notificationService.handleBookingConfirmed(event);
    }
}
