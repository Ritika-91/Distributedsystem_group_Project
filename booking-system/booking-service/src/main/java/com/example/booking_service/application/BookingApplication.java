package com.example.booking_service.application;

import com.example.booking_service.domain.Booking;
import com.example.booking_service.domain.BookingRepository;
import com.example.booking_service.domain.BookingStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingApplication {

    private final BookingRepository bookingRepository;

    public BookingApplication(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // ---------------------
    // CREATE BOOKING (NEW)
    // ---------------------
    public Booking createBooking(Long userId,
                                 Long roomId,
                                 LocalDateTime startTime,
                                 LocalDateTime endTime) {

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setRoomId(roomId);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    // ---------------------
    // GET ALL BOOKINGS
    // ---------------------
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // ---------------------
    // GET BOOKING BY ID
    // ---------------------
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    // ---------------------
    // GET BOOKINGS BY USER
    // ---------------------
    public List<Booking> getBookingsForUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    // ---------------------
    // GET BOOKINGS BY ROOM (NEW)
    // ---------------------
    public List<Booking> getBookingsForRoom(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    // ---------------------
    // CONFIRM BOOKING
    // ---------------------
    public Optional<Booking> confirmBooking(Long id) {
        return bookingRepository.findById(id).map(booking -> {
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setUpdatedAt(LocalDateTime.now());
                return bookingRepository.save(booking);
            }
            return booking;
        });
    }

    // ---------------------
    // CANCEL BOOKING (NEW)
    // ---------------------
    public Optional<Booking> cancelBooking(Long id, String reason) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason(reason);
            booking.setUpdatedAt(LocalDateTime.now());
            return bookingRepository.save(booking);
        });
    }
}

