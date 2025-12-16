package com.example.availability.dto;

public class AvailableRoomDto {

    private String roomId;
    private String roomName;
    private String roomType;
    private int capacity;

    public AvailableRoomDto() {
    }

    public AvailableRoomDto(String roomId, String roomName, String roomType, int capacity) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
        this.capacity = capacity;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
