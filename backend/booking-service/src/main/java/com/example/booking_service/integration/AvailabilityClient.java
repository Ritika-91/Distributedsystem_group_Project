package com.example.booking_service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AvailabilityClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public AvailabilityClient(@Value("${availability.baseUrl}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public JsonNode lock(Long roomId, Long bookingId, Long userId,
                         String start, String end) {

        Map<String, Object> body = new HashMap<>();
        body.put("roomId", String.valueOf(roomId));
        body.put("bookingId", bookingId);
        body.put("userId", userId);
        body.put("start", start);
        body.put("end", end);
        body.put("requestId", UUID.randomUUID().toString());

        return restTemplate.postForObject(
                baseUrl + "/availability/lock",
                body,
                JsonNode.class
        );
    }

    public JsonNode confirm(String lockId, Long bookingId) {
        Map<String, Object> body = new HashMap<>();
        body.put("lockId", lockId);
        body.put("bookingId", bookingId);

        return restTemplate.postForObject(
                baseUrl + "/availability/confirm",
                body,
                JsonNode.class
        );
    }

    public void release(String lockId, Long bookingId, String reason) {
        Map<String, Object> body = new HashMap<>();
        body.put("lockId", lockId);
        body.put("bookingId", bookingId);
        body.put("reason", reason);

        restTemplate.postForObject(
                baseUrl + "/availability/release",
                body,
                Void.class
        );
    }
}
