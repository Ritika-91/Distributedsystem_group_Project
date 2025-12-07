package com.example.booking_service.api;

import com.example.booking_service.api.dto.CreateBookingRequest;
import com.example.booking_service.api.dto.CancelBookingRequest;
import com.example.booking_service.application.BookingApplication;
import com.example.booking_service.domain.Booking;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingApplication bookingService;

    public BookingController(BookingApplication bookingService) {
        this.bookingService = bookingService;
    }

    // CREATE BOOKING
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {

        Booking created = bookingService.createBooking(
                request.getUserId(),
                request.getRoomId(),
                request.getStartTime(),
                request.getEndTime()
        );

        return ResponseEntity
                .created(URI.create("/bookings/" + created.getId()))
                .body(created);
    }

    // GET ALL BOOKINGS
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    // GET BOOKING BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        return bookingOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET BOOKINGS BY USER
    @GetMapping("/user/{userId}")
    public List<Booking> getBookingsForUser(@PathVariable Long userId) {
        return bookingService.getBookingsForUser(userId);
    }

    // OPTIONAL: GET BOOKINGS BY ROOM (useful for availability logic)
    @GetMapping("/room/{roomId}")
    public List<Booking> getBookingsForRoom(@PathVariable Long roomId) {
        return bookingService.getBookingsForRoom(roomId);
    }

    // CONFIRM BOOKING (PENDING -> CONFIRMED)
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirmBooking(@PathVariable Long id) {
        return bookingService.confirmBooking(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // CANCEL BOOKING WITH OPTIONAL REASON
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request) {

        String reason = (request != null) ? request.getReason() : null;

        return bookingService.cancelBooking(id, reason)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

