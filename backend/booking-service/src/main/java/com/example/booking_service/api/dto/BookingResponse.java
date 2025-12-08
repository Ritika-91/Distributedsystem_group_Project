package com.example.booking_service.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.booking_service.domain.BookingStatus;
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long roomId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private BookingStatus status;

    public Long getId(){ 
        return id; 
    }
    public void setId(Long id){ 
        this.id = id; 
    }

    public Long getUserId(){ 
        return userId; 
    }
    public void setUserId(Long userId){ 
        this.userId = userId; 
    }

    public Long getRoomId(){ 
        return roomId; 
    }
    public void setRoomId(Long roomId){ 
        this.roomId = roomId; 
    }

    public LocalDateTime getStartTime(){ 
        return startTime; 
    }
    public void setStartTime(LocalDateTime startTime){ 
        this.startTime = startTime; 
    }

    public LocalDateTime getEndTime(){ 
        return endTime; 
    }
    public void setEndTime(LocalDateTime endTime){ 
        this.endTime = endTime; 
    }

    public LocalDate getCheckInDate(){ 
        return checkInDate; 
    }
    public void setCheckInDate(LocalDate checkInDate){ 
        this.checkInDate = checkInDate; 
    }
    public LocalDate getCheckOutDate(){ 
        return checkOutDate; 
    }
    public void setCheckOutDate(LocalDate checkOutDate){ 
        this.checkOutDate = checkOutDate; 
    }
    public BookingStatus getStatus(){ 
        return status; 
    }
    public void setStatus(BookingStatus status){ 
        this.status = status; 
    }
}
