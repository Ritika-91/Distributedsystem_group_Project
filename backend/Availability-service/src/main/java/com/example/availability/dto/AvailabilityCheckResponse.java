package com.example.availability.dto;

public class AvailabilityCheckResponse {

    private String roomId;
    private String start;
    private String end;
    private boolean available;

    public AvailabilityCheckResponse() {
    }

    public AvailabilityCheckResponse(String roomId, String start, String end, boolean available) {
        this.roomId = roomId;
        this.start = start;
        this.end = end;
        this.available = available;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
