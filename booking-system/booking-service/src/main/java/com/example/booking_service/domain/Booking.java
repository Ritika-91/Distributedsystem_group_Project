//Reference: https://dev.to/wkreuch/create-an-entity-and-repository-using-spring-boot-3-2l6
package com.example.booking_service.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long roomId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    public Booking(){ }

    public Booking(Long userId, Long roomId, BookingStatus status, LocalDate checkInDate,LocalDate checkOutDate){
        this.userId=userId;
        this.roomId=roomId;
        this.status=status;
        this.checkInDate=checkInDate;
        this.checkOutDate=checkOutDate;
    }

    public Long getId(){
        return id;
    }

    public Long getUserId(){
        return userId;
    }
    public Long getRoomId(){
        return roomId;
    }
    public BookingStatus getStatus(){
        return status;
    }
    public LocalDate getCheckInDate(){
        return checkInDate;
    }
    public LocalDate getCheckOutDate(){
        return checkOutDate;
    }

    public void setUserId(Long userId){
         this.userId=userId;
    }
    public void setRoomId(Long roomId){
        this.roomId=roomId;
    }
    public void confirm(){
        this.status=BookingStatus.CONFIRMED;
    }
    public void cancel(){
        this.status=BookingStatus.CANCELLED;
    }
    public void setCheckInDate(LocalDate checkInDate){
        this.checkInDate=checkInDate;
    }
    public void setCheckOutDate(LocalDate checkOutDate){
        this.checkOutDate=checkOutDate;
    }
}

