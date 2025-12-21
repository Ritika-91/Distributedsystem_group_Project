package com.example.booking_service.api;

import com.example.booking_service.api.dto.AvailabilityResponse;
import com.example.booking_service.api.dto.CancelBookingRequest;
import com.example.booking_service.api.dto.CreateBookingRequest;
import com.example.booking_service.api.dto.BookingResponse;

import com.example.booking_service.application.BookingApplication;
import com.example.booking_service.domain.Booking;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.booking_service.domain.BookingStatus;
import com.example.booking_service.security.JwtAuth;
import com.example.booking_service.service.BookingService;


@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final BookingApplication bookingApplication;
    private final JwtAuth jwtAuth;

    public BookingController(BookingService bookingService,BookingApplication bookingApplication,JwtAuth jwtAuth) 
    {
        this.bookingService = bookingService;
        this.bookingApplication = bookingApplication;
        this.jwtAuth = jwtAuth;
    }
    @PostMapping
    public ResponseEntity<?> createBooking( @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreateBookingRequest request) 
        {
        Long userId = jwtAuth.requireUserId(authorization);
        try {
            BookingResponse created = bookingService.createBooking(userId, request);

            return ResponseEntity
                    .created(URI.create("/bookings/" + created.getId()))
                    .body(created);
        } 
        catch (IllegalStateException e) 
        {
            return ResponseEntity.status(409).body(new AvailabilityResponse(request.getRoomId(),false,e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        JwtAuth.UserPrincipal principal = jwtAuth.requirePrincipal(authorization);

        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {

        JwtAuth.UserPrincipal principal = jwtAuth.requirePrincipal(authorization);

        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isEmpty()) 
        {
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


    // GET MY BOOKINGS (USER)
    @GetMapping("/me")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Long userId = jwtAuth.requireUserId(authorization);
        return ResponseEntity.ok(bookingService.getBookingsForUser(userId));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Booking>> getBookingsForRoom(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long roomId) {

        JwtAuth.UserPrincipal principal = jwtAuth.requirePrincipal(authorization);

        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(bookingService.getBookingsForRoom(roomId));
    }

    // CHECK AVAILABILITY (PUBLIC/AUTH)
    @GetMapping("/availability/{roomId}")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @PathVariable Long roomId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime
    ) {
        boolean available = bookingApplication.isRoomAvailable(roomId, startTime, endTime);

        return ResponseEntity.ok(new AvailabilityResponse(roomId,available, available ? "Room is available" : "Room is already booked for the selected time"));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirmBooking(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @PathVariable Long id) {

    JwtAuth.UserPrincipal principal = jwtAuth.requirePrincipal(authorization);
    System.out.println("CONFIRM id=" + id + ", principal.userId=" + principal.userId());

    Optional<Booking> bookingOpt = bookingService.getBookingById(id);
    if (bookingOpt.isEmpty()) 
    {
        return ResponseEntity.notFound().build();
    }

    Booking booking = bookingOpt.get();
    System.out.println("CONFIRM: booking.userId=" + booking.getUserId());

    boolean isOwner = booking.getUserId() != null && booking.getUserId().equals(principal.userId());

    boolean isAdmin ="ADMIN".equalsIgnoreCase(principal.role());

    if (!isOwner && !isAdmin) 
    {
        System.out.println("CONFIRM: returning 403");
        return ResponseEntity.status(403).build();
    }

    return bookingService.confirmBooking(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
}

    // CANCEL BOOKING (OWNER OR ADMIN)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request) {

        JwtAuth.UserPrincipal principal = jwtAuth.requirePrincipal(authorization);

        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isEmpty()) 
        {
            return ResponseEntity.notFound().build();
        }

        Booking booking = bookingOpt.get();
        boolean isOwner =booking.getUserId() != null && booking.getUserId().equals(principal.userId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(principal.role());

        if (!isOwner && !isAdmin) 
        {
            return ResponseEntity.status(403).build();
        }
        String reason = (request != null) ? request.getReason() : null;
        return bookingService.cancelBooking(id, reason).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // USER confirms from waitlist when a slot opens
@PostMapping("/{id}/confirm-from-waitlist")
public ResponseEntity<Booking> confirmFromWaitlist(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @PathVariable Long id) {

    JwtAuth.UserPrincipal principal = jwtAuth.requirePrincipal(authorization);

    Optional<Booking> bookingOpt = bookingService.getBookingById(id);
    if (bookingOpt.isEmpty()) 
    {
        return ResponseEntity.notFound().build();
    }

    Booking booking = bookingOpt.get();
    if (!booking.getUserId().equals(principal.userId())) 
    {
        return ResponseEntity.status(403).build();
    }
    if (booking.getStatus() != BookingStatus.WAITLISTED) {
        return ResponseEntity.status(409).body(booking);
    }
    boolean slotTaken = bookingService.existsActiveBookingForSlot(
            booking.getRoomId(),
            booking.getStartTime(),
            booking.getEndTime()
    );
    if (slotTaken) 
    {
        return ResponseEntity.status(409).body(booking);
    }
    Booking confirmed = bookingService.confirmWaitlistedBooking(booking);
    return ResponseEntity.ok(confirmed);
}

}
