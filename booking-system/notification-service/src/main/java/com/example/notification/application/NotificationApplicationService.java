package com.example.notification.application;

import com.example.notification.events.BookingConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationApplicationService.class);

    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        // Here you could:
        // - create a Notification entity
        // - save to DB
        // - send email/SMS via external provider
        log.info("📩 Handling booking confirmation. Sending notification for bookingId={}, userId={}, roomId={}",
                event.getBookingId(), event.getUserId(), event.getRoomId());
    }
}
