package com.example.flashsale.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitServiceTest {

    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);

    @Test
    void redisCountsRequestsAndSetsExpiry() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L);
        when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(true);

        RateLimitService service = new RateLimitService(2, "redis", redisTemplate);

        assertTrue(service.isAllowed("10.0.0.1"));
        assertTrue(service.isAllowed("10.0.0.1"));
        assertFalse(service.isAllowed("10.0.0.1"));

        verify(redisTemplate, times(1)).expire(anyString(), any(Duration.class));
        verify(valueOperations, times(3)).increment(anyString());
    }

    @Test
    void fallsBackToMemoryWhenRedisFails() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("Redis down"));

        RateLimitService service = new RateLimitService(2, "redis", redisTemplate);

        assertTrue(service.isAllowed("10.0.0.2"));
        assertTrue(service.isAllowed("10.0.0.2"));
        assertFalse(service.isAllowed("10.0.0.2"));
    }
}
