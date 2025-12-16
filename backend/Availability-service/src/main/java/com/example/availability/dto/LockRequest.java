package com.example.availability.dto;

import jakarta.validation.constraints.NotNull;

public class LockRequest {

    @NotNull
    private String roomId;

    @NotNull
    private String start;  // ISO-8601 string

    @NotNull
    private String end;    // ISO-8601 string

    @NotNull
    private Long userId;

    @NotNull
    private String requestId;

    public LockRequest() {
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
