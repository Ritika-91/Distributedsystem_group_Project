package com.example.availability.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.availability.domain.AvailabilitySlot;
import com.example.availability.domain.SlotStatus;

public interface AvailabilitySlotRepository extends MongoRepository<AvailabilitySlot, String> {

    // All slots for a room that overlap [start, end)
    List<AvailabilitySlot> findByRoomIdAndEndTimeGreaterThanAndStartTimeLessThan(
            String roomId,
            Instant startExclusive,
            Instant endExclusive
    );

    Optional<AvailabilitySlot> findByLockId(String lockId);

    List<AvailabilitySlot> findByRoomIdAndStatus(String roomId, SlotStatus status);
}
