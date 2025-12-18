package com.example.booking_service.application;

import com.example.booking_service.domain.Booking;
import com.example.booking_service.domain.BookingRepository;
import com.example.booking_service.domain.BookingStatus;

import com.example.booking_service.integration.AvailabilityClient;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        booking= bookingRepository.save(booking);

        var lockResp = availabilityClient.lock(
            roomId,
            booking.getId(),
            userId,
            startTime.toString(),
            endTime.toString()
    );
   if (!lockResp.get("locked").asBoolean()) {
    booking.setStatus(BookingStatus.CANCELLED);
    booking.setUpdatedAt(LocalDateTime.now());
    return bookingRepository.save(booking);
}

    String lockId = lockResp.get("lockId").asText();
    booking.setLockId(lockId);
    booking.setStatus(BookingStatus.LOCKED);
    booking.setUpdatedAt(LocalDateTime.now());
    bookingRepository.save(booking);

    var confirmResp = availabilityClient.confirm(lockId, booking.getId());

    if (confirmResp.get("confirmed").asBoolean()) {
        
        booking.setStatus(BookingStatus.CONFIRMED);
    } else {
         availabilityClient.release(lockId, booking.getId(), "confirm_failed");
        booking.setStatus(BookingStatus.CANCELLED);
    }
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
            if (booking.getStatus() == BookingStatus.LOCKED) {
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

