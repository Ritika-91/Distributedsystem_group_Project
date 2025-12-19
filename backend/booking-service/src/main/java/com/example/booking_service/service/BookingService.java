package com.example.booking_service.service;

import com.example.booking_service.api.dto.BookingResponse;
import com.example.booking_service.api.dto.CreateBookingRequest;
import com.example.booking_service.domain.Booking;
import com.example.booking_service.domain.BookingRepository;
import com.example.booking_service.domain.BookingStatus;
import com.example.booking_service.integration.AvailabilityClient;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilityClient availabilityClient;

    public BookingService(BookingRepository bookingRepository,
                          AvailabilityClient availabilityClient) {
        this.bookingRepository = bookingRepository;
        this.availabilityClient = availabilityClient;
    }

    // ---------------------
    // CREATE BOOKING (DTO)
    // ---------------------
    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest request) {

        // 1) Validate
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // 2) Local overlap check (fail fast)
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
                        request.getEndTime(),
                        request.getStartTime()
                );

        // If overlap exists, we WAITLIST (your chosen behaviour)
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

        // 3) Create booking first (PENDING)
        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking(
                userId,
                request.getRoomId(),
                BookingStatus.PENDING,
                request.getStartTime(),
                request.getEndTime(),
                now,
                now,
                null, // lockId set after lock
                null,
                null,
                null,
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        Booking saved = bookingRepository.save(booking);

        // 4) Try to lock in Availability
        JsonNode lockResp = availabilityClient.lock(
                saved.getRoomId(),
                saved.getId(),
                userId,
                saved.getStartTime().toString(),
                saved.getEndTime().toString()
        );

        boolean locked = lockResp.hasNonNull("locked") && lockResp.get("locked").asBoolean();
        String lockId = lockResp.hasNonNull("lockId") ? lockResp.get("lockId").asText() : null;

        if (!locked || lockId == null) {
            // Lock failed => WAITLISTED (your current design)
            saved.setStatus(BookingStatus.WAITLISTED);
            saved.setCancellationReason("Room unavailable for selected time");
            saved.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(saved);
            return toResponse(saved);
        }

        // 5) Lock succeeded => LOCKED
        saved.setLockId(lockId);
        saved.setStatus(BookingStatus.LOCKED);
        saved.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(saved);

        return toResponse(saved);
    }

    // ---------------------
    // AVAILABILITY CHECK
    // ---------------------
    public boolean isRoomAvailable(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<BookingStatus> blockingStatuses = Arrays.asList(
                BookingStatus.REQUESTED,
                BookingStatus.PENDING,
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
    // GET MY BOOKINGS (DTO list)
    // ---------------------
    public List<BookingResponse> getBookingsForUser(Long userId) {
        return bookingRepository.findByUserIdAndStatusNot(userId, BookingStatus.CANCELLED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------------------
    // GET ALL BOOKINGS (entity list) - admin
    // ---------------------
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // ---------------------
    // GET BOOKING BY ID (entity)
    // ---------------------
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    // ---------------------
    // GET BOOKINGS BY ROOM (entity list) - admin
    // ---------------------
    public List<Booking> getBookingsForRoom(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    // ---------------------
    // CONFIRM BOOKING (admin style: id only)
    // sets LOCKED -> CONFIRMED
    // ---------------------
    @Transactional
    public Optional<Booking> confirmBooking(Long bookingId) {
        return bookingRepository.findById(bookingId).map(b -> {
            if (b.getStatus() == BookingStatus.LOCKED) {
                // Confirm in Availability if lockId exists
                if (b.getLockId() != null && !b.getLockId().isBlank()) {
                    availabilityClient.confirm(b.getLockId(), b.getId());
                }
                b.setStatus(BookingStatus.CONFIRMED);
                b.setUpdatedAt(LocalDateTime.now());
                return bookingRepository.save(b);
            }
            return b;
        });
    }

    // ---------------------
    // CANCEL BOOKING (controller expects Optional<Booking>)
    // ---------------------
    @Transactional
    public Optional<Booking> cancelBooking(Long id, String reason) {
        return bookingRepository.findById(id).map(booking -> {

            if (booking.getLockId() != null && !booking.getLockId().isBlank()) {
                try {
                    availabilityClient.release(
                            booking.getLockId(),
                            booking.getId(),
                            (reason != null ? reason : "cancelled_by_user")
                    );
                } catch (Exception ignored) {
                }
            }

            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason(reason);
            booking.setUpdatedAt(LocalDateTime.now());
            return bookingRepository.save(booking);
        });
    }

    // ---------------------
    // Mapper
    // ---------------------
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
