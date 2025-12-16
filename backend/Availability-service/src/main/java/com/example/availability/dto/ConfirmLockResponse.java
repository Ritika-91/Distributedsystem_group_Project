package com.example.availability.dto;

public class ConfirmLockResponse {

    private boolean confirmed;
    private String reason;

    public ConfirmLockResponse() {
    }

    public ConfirmLockResponse(boolean confirmed, String reason) {
        this.confirmed = confirmed;
        this.reason = reason;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
