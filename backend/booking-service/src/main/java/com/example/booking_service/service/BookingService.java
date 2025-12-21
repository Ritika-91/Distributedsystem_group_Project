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

    public BookingService(BookingRepository bookingRepository,AvailabilityClient availabilityClient) {
        this.bookingRepository = bookingRepository;
        this.availabilityClient = availabilityClient;
    }

//    Creating booking
    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) 
        {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) 
        {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        List<BookingStatus> activeStatuses = List.of(BookingStatus.REQUESTED,BookingStatus.PENDING,BookingStatus.LOCKED,BookingStatus.CONFIRMED);
        List<Booking> overlapping = bookingRepository.findByRoomIdAndStatusInAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            request.getRoomId(),
            activeStatuses,
            request.getEndTime(),
            request.getStartTime());
        if (!overlapping.isEmpty()) {
            Booking waitlisted = new Booking(userId,request.getRoomId(),BookingStatus.WAITLISTED,request.getStartTime(),request.getEndTime(),LocalDateTime.now(),LocalDateTime.now(),null, "Room unavailable, user placed on waitlist",null,null,request.getCheckInDate(),request.getCheckOutDate());
            return toResponse(bookingRepository.save(waitlisted));
        }
        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking(userId,request.getRoomId(),BookingStatus.PENDING,request.getStartTime(),request.getEndTime(),now,now,null, null,null,null,request.getCheckInDate(),request.getCheckOutDate());
        Booking saved = bookingRepository.save(booking);
        JsonNode lockResp = availabilityClient.lock(saved.getRoomId(),saved.getId(),userId,saved.getStartTime().toString(),saved.getEndTime().toString()
        );

        boolean locked = lockResp.hasNonNull("locked") && lockResp.get("locked").asBoolean();
        String lockId = lockResp.hasNonNull("lockId") ? lockResp.get("lockId").asText() : null;

        if (!locked || lockId == null) {
        //    if locked , other users waitlisted
            saved.setStatus(BookingStatus.WAITLISTED);
            saved.setCancellationReason("Room unavailable for selected time");
            saved.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(saved);
            return toResponse(saved);
        }

        saved.setLockId(lockId);
        saved.setStatus(BookingStatus.LOCKED);
        saved.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(saved);

        return toResponse(saved);
    }
    public boolean isRoomAvailable(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<BookingStatus> blockingStatuses = Arrays.asList(
            BookingStatus.REQUESTED,
            BookingStatus.PENDING,
            BookingStatus.LOCKED,
            BookingStatus.CONFIRMED
        );

        return bookingRepository.findByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(roomId,blockingStatuses,endTime,startTime).isEmpty();
    }
    public List<BookingResponse> getBookingsForUser(Long userId) {
        return bookingRepository.findByUserIdAndStatusNot(userId, BookingStatus.CANCELLED)
            .stream()
            .map(this::toResponse)
            .toList();
    }
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }
    public List<Booking> getBookingsForRoom(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }
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

        // 1) cancel current booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        if (booking.getStartTime().isAfter(LocalDateTime.now().plusMinutes(15))) {
            List<Booking> waitlisted = bookingRepository
                    .findByRoomIdAndStartTimeAndEndTimeAndStatusOrderByCreatedAtAsc(
                        booking.getRoomId(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        BookingStatus.WAITLISTED
                    );

            if (!waitlisted.isEmpty()) {
                Booking firstInLine = waitlisted.get(0);
                System.out.println("Waitlist offer: user " + firstInLine.getUserId()+ " for room " + firstInLine.getRoomId());
            }
        }
        return saved;
    });
}
public boolean existsActiveBookingForSlot(Long roomId, LocalDateTime start, LocalDateTime end) {
    List<BookingStatus> blockingStatuses = List.of(BookingStatus.REQUESTED, BookingStatus.PENDING,BookingStatus.LOCKED, BookingStatus.CONFIRMED);

    return !bookingRepository.findByRoomIdAndStatusInAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(roomId,blockingStatuses,end,start).isEmpty();
}

@Transactional
public Booking confirmWaitlistedBooking(Booking booking) {
    booking.setStatus(BookingStatus.CONFIRMED);
    booking.setCancellationReason(null);
    booking.setUpdatedAt(LocalDateTime.now());
    return bookingRepository.save(booking);
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
