package com.example.flashsale.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private final int requestsPerMinute;
    private final boolean redisEnabled;
    private final StringRedisTemplate redisTemplate;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitService(
            @Value("${app.rate-limit.requests-per-minute:60}") int requestsPerMinute,
            @Value("${app.rate-limit.backend:redis}") String backend,
            StringRedisTemplate redisTemplate) {
        this.requestsPerMinute = requestsPerMinute;
        this.redisEnabled = "redis".equalsIgnoreCase(backend);
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String client) {
        if (redisEnabled && redisTemplate != null) {
            try {
                return isAllowedByRedis(client);
            } catch (RuntimeException ex) {
                log.warn("Redis rate limiting unavailable, using in-memory fallback: {}",
                        ex.getMessage());
            }
        }

        return isAllowedInMemory(client);
    }

    private boolean isAllowedByRedis(String client) {
        long currentMinute = System.currentTimeMillis() / 60_000;
        String key = "rate_limit:" + client + ":" + currentMinute;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(2));
        }

        return count != null && count <= requestsPerMinute;
    }

    private boolean isAllowedInMemory(String client) {
        long currentMinute = System.currentTimeMillis() / 60_000;
        Window window = windows.computeIfAbsent(client, key -> new Window(currentMinute));
        int currentCount;

        synchronized (window) {
            if (window.minute != currentMinute) {
                window.minute = currentMinute;
                window.count = 0;
            }
            window.count++;
            currentCount = window.count;
        }

        return currentCount <= requestsPerMinute;
    }

    private static class Window {
        private long minute;
        private int count;

        private Window(long minute) {
            this.minute = minute;
        }
    }
}
