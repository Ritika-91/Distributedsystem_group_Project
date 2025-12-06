package com.example.booking_service.api;

import com.example.booking_service.application.BookingApplication;
import com.example.booking_service.domain.Booking;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingApplication bookingService;
    public BookingController(BookingApplication bookingService){
        this.bookingService=bookingService;
    }

    public static class CreateBookingRequest{
        public Long userId;
        public Long roomId;
        public String checkInDate;
        public String checkOutDate;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody CreateBookingRequest request){
        LocalDate checkIn = LocalDate.parse(request.checkInDate);
        LocalDate checkOut = LocalDate.parse(request.checkOutDate);

        Booking created=bookingService.createBooking(request.userId, request.roomId, checkIn, checkOut);
        return ResponseEntity.created(URI.create("/bookings/"+created.getId())).body(created);

    }
     @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id){
        Optional<Booking> bookingOpt= bookingService.getBookingById(id);
        return bookingOpt.map(ResponseEntity::ok).orElseGet(()-> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getBookingsForUser(@PathVariable Long userId) {
        return bookingService.getBookingsForUser(userId);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirmBooking(@PathVariable Long id) {
        return bookingService.confirmBooking(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
