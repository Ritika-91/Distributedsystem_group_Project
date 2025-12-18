package com.example.booking_service.service;

import com.example.booking_service.api.dto.BookingResponse;
import com.example.booking_service.api.dto.CreateBookingRequest;
import com.example.booking_service.domain.*;
import com.example.booking_service.integration.AvailabilityClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilityClient availabilityClient;

    public BookingService(BookingRepository bookingRepository,
                          AvailabilityClient availabilityClient) {
        this.bookingRepository = bookingRepository;
        this.availabilityClient = availabilityClient;
    }

    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest request) {

        // 1) Validate
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // 2) (Optional but good) local overlap check to fail fast
        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.REQUESTED,
                BookingStatus.PENDING,
                BookingStatus.LOCKED,
                BookingStatus.CONFIRMED
        );

        List<Booking> overlapping = bookingRepository
                .findByRoomIdAndStatusInAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                        request.getRoomId(),
                        activeStatuses,
                        request.getEndTime(),   // startTime <= reqEnd
                        request.getStartTime()  // endTime >= reqStart
                );

        if (!overlapping.isEmpty()) {
            Booking waitlisted = new Booking(
                    userId,
                    request.getRoomId(),
                    BookingStatus.WAITLISTED,
                    request.getStartTime(),
                    request.getEndTime(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    null,
                    "Room unavailable, user placed on waitlist",
                    null,
                    null,
                    request.getCheckInDate(),
                    request.getCheckOutDate()
            );

            return toResponse(bookingRepository.save(waitlisted));
        }

        // 3) Create booking first (so we have bookingId to send to Availability)
        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking(
                userId,
                request.getRoomId(),
                BookingStatus.PENDING,
                request.getStartTime(),
                request.getEndTime(),
                now,
                now,
                null, // lockId (set after lock)
                null,
                null,
                null,
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        Booking saved = bookingRepository.save(booking);

        // 4) Try to lock in Availability Service
        JsonNode lockResp = availabilityClient.lock(
                saved.getRoomId(),
                saved.getId(),
                userId,
                saved.getStartTime().toString(),
                saved.getEndTime().toString()
        );

        // Expecting something like: { "status":"LOCKED", "lockId":"..." }
        String lockId = lockResp.hasNonNull("lockId") ? lockResp.get("lockId").asText() : null;
        String status = lockResp.hasNonNull("status") ? lockResp.get("status").asText() : null;

        if (lockId == null || !"LOCKED".equalsIgnoreCase(status)) {
            // Lock failed → mark booking WAITLISTED (or CANCELLED) to avoid dangling PENDING
            saved.setStatus(BookingStatus.WAITLISTED); 
            saved.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(saved);

            return toResponse(saved);
        }

        // 5) Lock succeeded → update booking to LOCKED and store lockId
        saved.setLockId(lockId);
        saved.setStatus(BookingStatus.LOCKED);
        saved.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(saved);

        return toResponse(saved);
    }

    public List<BookingResponse> getBookingsForUser(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // If we already have a lock, release it in Availability Service
        if (booking.getLockId() != null && !booking.getLockId().isBlank()) {
    try {
        availabilityClient.release(
                java.util.UUID.randomUUID().toString(),
                booking.getId(),
                booking.getLockId()
        );
    } catch (Exception ignored) {
    }
}


        booking.cancel();
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // TODO: publish to Kafka → notify users
    }

    private BookingResponse toResponse(Booking booking) {
        BookingResponse resp = new BookingResponse();
        resp.setId(booking.getId());
        resp.setUserId(booking.getUserId());
        resp.setRoomId(booking.getRoomId());
        resp.setStartTime(booking.getStartTime());
        resp.setEndTime(booking.getEndTime());
        resp.setCheckInDate(booking.getCheckInDate());
        resp.setCheckOutDate(booking.getCheckOutDate());
        resp.setStatus(booking.getStatus());
        return resp;
    }
}
