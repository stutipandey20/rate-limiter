package ratelimiter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SlidingWindowLogRateLimiter implements RateLimiter {
    private final int maxRequests;
    private final int windowSizeInSeconds;

    // Stores timestamp logs per user/IP
    private final Map<String, Queue<Long>> userLogs = new ConcurrentHashMap<>();

    public SlidingWindowLogRateLimiter(int maxRequests, int windowSizeInSeconds) {
        this.maxRequests = maxRequests;
        this.windowSizeInSeconds = windowSizeInSeconds;
    }
    
    @Override
    public synchronized boolean allowRequest (String key) {
        long currentTime = System.currentTimeMillis();
        long windowStartTime = currentTime - windowSizeInSeconds * 1000L;

        userLogs.putIfAbsent(key, new ConcurrentLinkedQueue<>());
        Queue<Long> timestamps = userLogs.get(key);

        while(! timestamps.isEmpty() && timestamps.peek() < windowStartTime) {
            timestamps.poll();
        }

        if (timestamps.size() < maxRequests) {
            timestamps.add(currentTime);
            return true;
        }

        // rate limit is exceeded
        return false;
    }
}
