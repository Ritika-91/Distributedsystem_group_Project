package com.example.booking_service.api.dto;

public class CancelBookingRequest {

    private String reason;
    public String getReason(){ 
        return reason; 
    }
    public void setReason(String reason){ 
        this.reason = reason; 
    }
}
