package com.example.booking_service.api;

import com.example.booking_service.api.dto.CancelBookingRequest;
import com.example.booking_service.api.dto.CreateBookingRequest;
import com.example.booking_service.application.BookingApplication;
import com.example.booking_service.domain.Booking;
import com.example.booking_service.security.JwtAuth;   // ✅ THIS IS THE FIX

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
    private final JwtAuth jwtAuth;

    public BookingController(BookingApplication bookingService, JwtAuth jwtAuth) {
        this.bookingService = bookingService;
        this.jwtAuth = jwtAuth;
    }

    // CREATE BOOKING
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreateBookingRequest request) {

        Long userId = jwtAuth.requireUserId(authorization);


        Booking created = bookingService.createBooking(
                userId,
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

    // GET BOOKING BY ID (OWNER OR ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {

        JwtAuth.UserPrincipal principal = jwtAuth.requirePrincipal(authorization);

        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Booking booking = bookingOpt.get();

        boolean isOwner =
                booking.getUserId() != null &&
                booking.getUserId().equals(principal.userId());

        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(principal.role());

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(booking);
    }

    // GET MY BOOKINGS
    @GetMapping("/me")
    public ResponseEntity<List<Booking>> getMyBookings(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long userId = jwtAuth.requireUserId(authorization);
        return ResponseEntity.ok(bookingService.getBookingsForUser(userId));
    }

    // GET BOOKINGS BY ROOM
    @GetMapping("/room/{roomId}")
    public List<Booking> getBookingsForRoom(@PathVariable Long roomId) {
        return bookingService.getBookingsForRoom(roomId);
    }

    // CONFIRM BOOKING (ADMIN ONLY)
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirmBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {

        JwtAuth.UserPrincipal principal = jwtAuth.requirePrincipal(authorization);

        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            return ResponseEntity.status(403).build();
        }

        return bookingService.confirmBooking(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // CANCEL BOOKING (OWNER OR ADMIN)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request) {

        JwtAuth.UserPrincipal principal = jwtAuth.requirePrincipal(authorization);

        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Booking booking = bookingOpt.get();

        boolean isOwner =
                booking.getUserId() != null &&
                booking.getUserId().equals(principal.userId());

        boolean isAdmin =
                "ADMIN".equalsIgnoreCase(principal.role());

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).build();
        }

        String reason = (request != null) ? request.getReason() : null;

        return bookingService.cancelBooking(id, reason)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
