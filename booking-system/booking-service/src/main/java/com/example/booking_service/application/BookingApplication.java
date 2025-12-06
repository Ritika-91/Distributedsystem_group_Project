package com.example.booking_service.application;

import com.example.booking_service.domain.Booking;
import com.example.booking_service.domain.BookingRepository;
import com.example.booking_service.domain.BookingStatus;
import com.example.booking_service.domain.events.BookingConfirmedEvent;
import com.example.booking_service.infrastructure.messaging.BookingEventPublisher;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
@Service
public class BookingApplication {
    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;

    public BookingApplication(BookingRepository bookingRepository,BookingEventPublisher eventPublisher){
        this.bookingRepository=bookingRepository;
        this.eventPublisher = eventPublisher;
    }

    public Booking createBooking(Long userId, Long roomId, LocalDate checkInDate,LocalDate checkOutDate){
        if(checkOutDate.isBefore(checkInDate)){
            throw new IllegalArgumentException("checkOutDate must be after checkindate");
        }
        Booking booking = new Booking(userId, roomId, BookingStatus.PENDING, checkInDate,checkOutDate);
        Booking saved = bookingRepository.save(booking);

        BookingConfirmedEvent event =
                new BookingConfirmedEvent(saved.getId(), saved.getUserId(), saved.getRoomId());

        eventPublisher.publishBookingConfirmed(event);

        return saved;
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsForUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }
    public Optional<Booking> confirmBooking(Long id) {
        return bookingRepository.findById(id).map(booking -> {
            booking.confirm();
            return bookingRepository.save(booking);
        });
    }
    public Optional<Booking> cancelBooking(Long id) {
        return bookingRepository.findById(id).map(booking -> {
            booking.cancel();
            return bookingRepository.save(booking);
        });
    }
}
