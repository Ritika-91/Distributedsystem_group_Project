package com.example.booking_service.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CreateBookingRequest {


    @NotNull
    private Long roomId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public Long getRoomId() 
    { 
        return roomId; 

    }
    public void setRoomId(Long roomId) 
    { 
        this.roomId = roomId; 

    }

    public LocalDateTime getStartTime() 
    {
         return startTime; 

    }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() 
    { 
        return endTime; 

    }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDate getCheckInDate() 
    { 
        return checkInDate; 

    }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() 
    { 
        return checkOutDate; 

    }
    public void setCheckOutDate(LocalDate checkOutDate) 
    { 
        this.checkOutDate = checkOutDate; 
    }
}
