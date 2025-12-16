package com.example.availability.dto;

public class LockResponse {

    private boolean locked;
    private String lockId;
    private String expiresAt;
    private String reason;

    public LockResponse() {
    }

    public LockResponse(boolean locked, String lockId, String expiresAt, String reason) {
        this.locked = locked;
        this.lockId = lockId;
        this.expiresAt = expiresAt;
        this.reason = reason;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
