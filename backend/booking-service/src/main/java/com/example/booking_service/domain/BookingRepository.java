package com.example.booking_service.domain;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;



public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdAndStatusNot(Long userId, BookingStatus status);
    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByRoomIdAndStatusInAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Long roomId,
            List<BookingStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    List<Booking> findByRoomIdAndStartTimeBetween(Long roomId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            List<BookingStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

List<Booking> findByRoomIdAndStartTimeAndEndTimeAndStatusOrderByCreatedAtAsc(
        Long roomId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BookingStatus status);

}
