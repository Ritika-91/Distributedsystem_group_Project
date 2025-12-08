package com.example.notification.events;

public class BookingConfirmedEvent {

    private Long bookingId;
    private Long userId;
    private Long roomId;
    public BookingConfirmedEvent() {
    }

    public BookingConfirmedEvent(Long bookingId, Long userId, Long roomId) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.roomId = roomId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRoomId() {
        return roomId;
    }

    @Override
    public String toString() {
        return "BookingConfirmedEvent{" +"bookingId=" + bookingId + ", userId=" + userId +", roomId=" + roomId +'}';
    }
}

