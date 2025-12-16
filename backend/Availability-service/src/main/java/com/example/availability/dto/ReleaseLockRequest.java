package com.example.availability.dto;

import jakarta.validation.constraints.NotNull;

public class ReleaseLockRequest {

    @NotNull
    private String lockId;

    @NotNull
    private Long bookingId;

    @NotNull
    private String reason;

    public ReleaseLockRequest() {
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
