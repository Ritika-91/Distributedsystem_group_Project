package com.example.availability.controller;

import com.example.availability.dto.*;
import com.example.availability.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    // 1. GET /availability?roomId=&start=&end=
    @GetMapping("/availability")
    public ResponseEntity<AvailabilityCheckResponse> checkAvailability(
            @RequestParam String roomId,
            @RequestParam String start,
            @RequestParam String end) {

        AvailabilityCheckResponse response =
                availabilityService.checkRoomAvailability(roomId, start, end);

        return ResponseEntity.ok(response);
    }

    // 2. GET /rooms/available?start=&end=&roomType=
    @GetMapping("/rooms/available")
    public ResponseEntity<List<AvailableRoomDto>> getAvailableRooms(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String roomType) {

        List<AvailableRoomDto> rooms =
                availabilityService.getAvailableRooms(start, end, roomType);

        return ResponseEntity.ok(rooms);
    }

    // 3. POST /availability/lock
    @PostMapping("/availability/lock")
    public ResponseEntity<LockResponse> lockRoom(@Valid @RequestBody LockRequest request) {
        LockResponse response = availabilityService.lockRoom(request);
        return ResponseEntity.ok(response);
    }

    // 4. POST /availability/confirm
    @PostMapping("/availability/confirm")
    public ResponseEntity<ConfirmLockResponse> confirmLock(
            @Valid @RequestBody ConfirmLockRequest request) {
        ConfirmLockResponse response = availabilityService.confirmLock(request);
        return ResponseEntity.ok(response);
    }

    // 5. POST /availability/release
    @PostMapping("/availability/release")
    public ResponseEntity<ReleaseLockResponse> releaseLock(
            @Valid @RequestBody ReleaseLockRequest request) {
        ReleaseLockResponse response = availabilityService.releaseLock(request);
        return ResponseEntity.ok(response);
    }
}
