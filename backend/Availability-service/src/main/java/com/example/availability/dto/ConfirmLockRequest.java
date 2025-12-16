package com.example.availability.dto;

import jakarta.validation.constraints.NotNull;

public class ConfirmLockRequest {

    @NotNull
    private String lockId;

    @NotNull
    private Long bookingId;

    public ConfirmLockRequest() {
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
}
