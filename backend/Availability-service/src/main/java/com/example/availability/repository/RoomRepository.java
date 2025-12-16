package com.example.availability.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.availability.domain.Room;

public interface RoomRepository extends MongoRepository<Room, String> {

    List<Room> findByType(String type);
}
