package com.studentsupport.service;

import com.studentsupport.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimiterServiceTest {

    private final RateLimiterService rateLimiter = new RateLimiterService();

    @Test
    void allowsRequestsWithinLimit() {
        String key = "test-key-" + System.nanoTime();
        for (int i = 0; i < 5; i++) {
            assertThatCode(() -> rateLimiter.checkAllowed(key, 5, Duration.ofMinutes(1)))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void blocksRequestsOverLimit() {
        String key = "test-key-" + System.nanoTime();
        for (int i = 0; i < 3; i++) {
            rateLimiter.checkAllowed(key, 3, Duration.ofMinutes(1));
        }

        assertThatThrownBy(() -> rateLimiter.checkAllowed(key, 3, Duration.ofMinutes(1)))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void differentKeysAreTrackedIndependently() {
        String keyA = "key-a-" + System.nanoTime();
        String keyB = "key-b-" + System.nanoTime();

        rateLimiter.checkAllowed(keyA, 1, Duration.ofMinutes(1));

        assertThatCode(() -> rateLimiter.checkAllowed(keyB, 1, Duration.ofMinutes(1)))
                .doesNotThrowAnyException();
    }

    @Test
    void oldAttemptsOutsideWindowAreForgotten() {
        String key = "test-key-" + System.nanoTime();
        // A window of ~0ms means every prior attempt is immediately "outside" the window,
        // so repeated calls should never be blocked.
        for (int i = 0; i < 5; i++) {
            assertThatCode(() -> rateLimiter.checkAllowed(key, 1, Duration.ofMillis(1)))
                    .doesNotThrowAnyException();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
