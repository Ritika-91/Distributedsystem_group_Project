package com.example.availability.dto;

public class ReleaseLockResponse {

    private boolean released;
    private String reason;

    public ReleaseLockResponse() {
    }

    public ReleaseLockResponse(boolean released, String reason) {
        this.released = released;
        this.reason = reason;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
