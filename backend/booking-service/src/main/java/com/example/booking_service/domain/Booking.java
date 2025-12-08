//Reference: https://dev.to/wkreuch/create-an-entity-and-repository-using-spring-boot-3-2l6
package com.example.booking_service.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long roomId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String lockId;

    private String cancellationReason;

    private String roomName;
    private String roomType;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    public Booking(){ }

    public Booking(Long userId, Long roomId, BookingStatus status, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime createdAt, LocalDateTime updatedAt, String lockId, String cancellationReason, String roomName, String roomType,  LocalDate checkInDate,LocalDate checkOutDate){
        this.userId=userId;
        this.roomId=roomId;
        this.status=status;
        this.startTime=startTime;
        this.endTime=endTime;
        this.createdAt=createdAt;
        this.updatedAt=updatedAt;
        this.lockId=lockId;
        this.roomName=roomName;
        this.roomType=roomType;
        this.cancellationReason=cancellationReason;
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
    public LocalDateTime getStartTime(){
        return startTime;
    }
      public LocalDateTime getEndTime(){
        return endTime;
    }
    public String getLockId(){
        return lockId;
    }
      public LocalDateTime getCreatedAt(){
        return createdAt;
    }
      public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }
    public String getCancellationReason(){
        return cancellationReason;
    }
     public String getRoomName(){
        return roomName;
    }
     public String getRoomType(){
        return roomType;
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
    public void setStartTime(LocalDateTime startTime){
        this.startTime=startTime;
    }
    public void setEndTime(LocalDateTime endTime){
        this.endTime=endTime;
    }
    public void setLockId(String lockId){
        this.lockId=lockId;
    }
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
}

