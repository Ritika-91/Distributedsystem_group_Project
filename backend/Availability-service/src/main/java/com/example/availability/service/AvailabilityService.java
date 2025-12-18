package com.example.availability.service;

import com.example.availability.domain.AvailabilitySlot;
import com.example.availability.domain.Room;
import com.example.availability.domain.SlotStatus;
import com.example.availability.dto.*;
import com.example.availability.repository.AvailabilitySlotRepository;
import com.example.availability.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.OffsetDateTime;

@Service
public class AvailabilityService {

    private final RoomRepository roomRepository;
    private final AvailabilitySlotRepository slotRepository;

    // e.g. locks are valid for 5 minutes before auto-expire
    private static final Duration DEFAULT_LOCK_TTL = Duration.ofMinutes(5);

    public AvailabilityService(RoomRepository roomRepository,
                               AvailabilitySlotRepository slotRepository) {
        this.roomRepository = roomRepository;
        this.slotRepository = slotRepository;
    }

    public AvailabilityCheckResponse checkRoomAvailability(String roomId,
                                                           String startIso,
                                                           String endIso) {
        Instant start = parseInstant(startIso);
        Instant end = parseInstant(endIso);

        boolean available = isRoomFree(roomId, start, end);

        return new AvailabilityCheckResponse(roomId, startIso, endIso, available);
    }

    public List<AvailableRoomDto> getAvailableRooms(String startIso,
                                                    String endIso,
                                                    String roomType) {
        Instant start = parseInstant(startIso);
        Instant end = parseInstant(endIso);

        List<Room> rooms = (roomType == null || roomType.isBlank())
                ? roomRepository.findAll()
                : roomRepository.findByType(roomType);

        return rooms.stream()
                .filter(r -> isRoomFree(r.getId(), start, end))
                .map(r -> new AvailableRoomDto(r.getId(), r.getName(), r.getType(), r.getCapacity()))
                .collect(Collectors.toList());
    }

    @Transactional
    public LockResponse lockRoom(LockRequest request) {
        Instant start = parseInstant(request.getStart());
        Instant end = parseInstant(request.getEnd());

        // basic sanity
        if (!start.isBefore(end)) {
            return new LockResponse(false, null, null, "INVALID_TIME_RANGE");
        }

        // check availability (ignore expired locks)
        if (!isRoomFree(request.getRoomId(), start, end)) {
            return new LockResponse(false, null, null, "ROOM_NOT_AVAILABLE");
        }

        // create lock slot
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setRoomId(request.getRoomId());
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setStatus(SlotStatus.LOCKED);
        slot.setUserId(request.getUserId());
        slot.setLockId(generateLockId(request));
        slot.setLockExpiresAt(Instant.now().plus(DEFAULT_LOCK_TTL));
        slot.setCreatedAt(Instant.now());
        slot.setUpdatedAt(Instant.now());

        slotRepository.save(slot);

        return new LockResponse(
                true,
                slot.getLockId(),
                slot.getLockExpiresAt().toString(),
                null
        );
    }

    @Transactional
    public ConfirmLockResponse confirmLock(ConfirmLockRequest request) {
        Optional<AvailabilitySlot> optional = slotRepository.findByLockId(request.getLockId());
        if (optional.isEmpty()) {
            return new ConfirmLockResponse(false, "LOCK_NOT_FOUND");
        }

        AvailabilitySlot slot = optional.get();

        // ensure it's still locked
        if (slot.getStatus() != SlotStatus.LOCKED) {
            return new ConfirmLockResponse(false, "LOCK_NOT_IN_LOCKED_STATE");
        }

        // check not expired
        if (slot.getLockExpiresAt() != null && Instant.now().isAfter(slot.getLockExpiresAt())) {
            // auto-release
            slot.setStatus(SlotStatus.FREE);
            slot.setLockId(null);
            slot.setLockExpiresAt(null);
            slot.setUpdatedAt(Instant.now());
            slotRepository.save(slot);
            return new ConfirmLockResponse(false, "LOCK_EXPIRED");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slot.setBookingId(request.getBookingId());
        slot.setUpdatedAt(Instant.now());
        slotRepository.save(slot);

        return new ConfirmLockResponse(true, null);
    }

    @Transactional
    public ReleaseLockResponse releaseLock(ReleaseLockRequest request) {
        Optional<AvailabilitySlot> optional = slotRepository.findByLockId(request.getLockId());
        if (optional.isEmpty()) {
            return new ReleaseLockResponse(false, "LOCK_NOT_FOUND");
        }

        AvailabilitySlot slot = optional.get();

        // basic consistency check: match bookingId if present
        if (slot.getBookingId() != null && !slot.getBookingId().equals(request.getBookingId())) {
            return new ReleaseLockResponse(false, "BOOKING_ID_MISMATCH");
        }

        // free the slot (you could also delete it instead)
        slot.setStatus(SlotStatus.FREE);
        slot.setLockId(null);
        slot.setBookingId(null);
        slot.setLockExpiresAt(null);
        slot.setUpdatedAt(Instant.now());
        slotRepository.save(slot);

        return new ReleaseLockResponse(true, null);
    }

    private boolean isRoomFree(String roomId, Instant start, Instant end) {
        // Find overlapping slots
        List<AvailabilitySlot> overlapping = slotRepository
                .findByRoomIdAndEndTimeGreaterThanAndStartTimeLessThan(roomId, start, end);

        // Treat expired locks as free
        Instant now = Instant.now();
        return overlapping.stream()
                .filter(s -> s.getStatus() == SlotStatus.BOOKED ||
                        (s.getStatus() == SlotStatus.LOCKED &&
                                (s.getLockExpiresAt() == null || now.isBefore(s.getLockExpiresAt()))))
                .findAny()
                .isEmpty();
    }

    private Instant parseInstant(String iso) {
        if (iso == null || iso.isBlank()) {
        throw new IllegalArgumentException("Datetime is missing");
    }

        try {
            return Instant.parse(iso);
        } 
        catch (DateTimeParseException ignored) {}
        try {
        return OffsetDateTime.parse(iso).toInstant();
    } catch (DateTimeParseException ignored) {}
        try {
        return LocalDateTime.parse(iso).toInstant(ZoneOffset.UTC);
    } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid ISO-8601 datetime: " + iso, e);
    }
    }

    private String generateLockId(LockRequest request) {
        return "LOCK-" + UUID.randomUUID();
    }
}
