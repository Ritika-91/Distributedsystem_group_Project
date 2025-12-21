package com.example.booking_service.api.dto;

public class AvailabilityResponse {

    private Long roomId;
    private boolean available;
    private String message;

    public AvailabilityResponse(Long roomId, boolean available, String message) {
        this.roomId = roomId;
        this.available = available;
        this.message = message;
    }

    public Long getRoomId() {
        return roomId;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getMessage() {
        return message;
    }
}
