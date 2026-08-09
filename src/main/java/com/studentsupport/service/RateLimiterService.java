package com.studentsupport.service;

import com.studentsupport.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Simple in-memory sliding-window rate limiter. Sufficient for a single-instance
 * deployment; would need a shared store (e.g. Redis) if scaled horizontally.
 */
@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String key, int maxAttempts, Duration window) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - window.toMillis();
        ConcurrentLinkedDeque<Long> timestamps = attempts.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxAttempts) {
                throw new TooManyRequestsException("Too many attempts. Please try again later.");
            }
            timestamps.addLast(now);
        }
    }
}
