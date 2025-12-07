package com.example.booking_service.service;

import com.example.booking_service.domain.*;
import com.example.booking_service.api.dto.CreateBookingRequest;
import com.example.booking_service.api.dto.BookingResponse;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public BookingResponse createBooking(CreateBookingRequest request) {

        // 1. Validate
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // 2. ACTIVE statuses: these block the room
        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.REQUESTED,
                BookingStatus.PENDING,
                BookingStatus.LOCKED,
                BookingStatus.CONFIRMED
        );

        // 3. Check overlap
        List<Booking> overlapping = bookingRepository
                .findByRoomIdAndStatusInAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                        request.getRoomId(),
                        activeStatuses,
                        request.getEndTime(),
                        request.getStartTime()
                );

        if (!overlapping.isEmpty()) {

            // Instead of throwing, later you can:
            // - Create WAITLIST entry
            // - Return WAITLISTED status
            Booking booking = new Booking(
                    request.getUserId(),
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

            Booking saved = bookingRepository.save(booking);
            return toResponse(saved);
        }

        // 4. Create booking with status = PENDING (until lock acquired)
        LocalDateTime now = LocalDateTime.now();

        Booking booking = new Booking(
                request.getUserId(),
                request.getRoomId(),
                BookingStatus.PENDING,
                request.getStartTime(),
                request.getEndTime(),
                now,
                now,
                null,
                null,
                null,
                null,
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        // Later: call LockService → change status to LOCKED → CONFIRMED
        Booking saved = bookingRepository.save(booking);

        return toResponse(saved);
    }

    public List<BookingResponse> getBookingsForUser(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

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
