package com.example.booking_service.application;

import com.example.booking_service.domain.Booking;
import com.example.booking_service.domain.BookingRepository;
import com.example.booking_service.domain.BookingStatus;
import com.example.booking_service.integration.AvailabilityClient;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingApplication {

    private final BookingRepository bookingRepository;
    private final AvailabilityClient availabilityClient;

    public BookingApplication(BookingRepository bookingRepository,
                              AvailabilityClient availabilityClient) {
        this.bookingRepository = bookingRepository;
        this.availabilityClient = availabilityClient;
    }

    
    // ---------------------
    // AVAILABILITY CHECK
    // ---------------------
    public boolean isRoomAvailable(Long roomId,
                                   LocalDateTime startTime,
                                   LocalDateTime endTime) {

        List<BookingStatus> blockingStatuses = Arrays.asList(
                BookingStatus.REQUESTED,
                BookingStatus.LOCKED,
                BookingStatus.CONFIRMED
        );

        return bookingRepository
                .findByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        roomId,
                        blockingStatuses,
                        endTime,
                        startTime
                )
                .isEmpty();
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
        return bookingRepository.findByUserIdAndStatusNot(
                userId,
                BookingStatus.CANCELLED
        );
    }

    // ---------------------
    // GET BOOKINGS BY ROOM
    // ---------------------
    public List<Booking> getBookingsForRoom(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    // ---------------------
    // CONFIRM BOOKING
    // ---------------------
    public Optional<Booking> confirmBooking(Long id) {
        return bookingRepository.findById(id).map(booking -> {
            if (booking.getStatus() == BookingStatus.LOCKED) {
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setUpdatedAt(LocalDateTime.now());
                return bookingRepository.save(booking);
            }
            return booking;
        });
    }

    // ---------------------
    // CANCEL BOOKING (USER ACTION)
    // ---------------------
    public Optional<Booking> cancelBooking(Long id, String reason) {
        return bookingRepository.findById(id).map(booking -> {

            if (booking.getLockId() != null) {
                availabilityClient.release(
                        booking.getLockId(),
                        booking.getId(),
                        reason
                );
            }

            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason(reason);
            booking.setUpdatedAt(LocalDateTime.now());
            return bookingRepository.save(booking);
        });
    }
}
