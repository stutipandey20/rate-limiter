package ratelimiter;

import redis.clients.jedis.Jedis;

import java.time.Instant;

public class RedisSlidingWindowRateLimiter implements RateLimiter {
    private final int maxRequests;
    private final int windowSizeInSeconds;
    private final Jedis jedis;

    public RedisSlidingWindowRateLimiter(int maxRequests, int windowSizeInSeconds) {
        this.maxRequests = maxRequests;
        this.windowSizeInSeconds = windowSizeInSeconds;
        this.jedis = new Jedis("localhost", 6379);
    }

    @Override
    public synchronized boolean allowRequest(String key) {
        long currentTimeMillis = System.currentTimeMillis();
        long currentWindowStart = currentTimeMillis / 1000 / windowSizeInSeconds * windowSizeInSeconds;
        long previousWindowStart = currentWindowStart - windowSizeInSeconds;

        String currentKey = "rate_limiter:" + key + ":" + currentWindowStart;
        String prevKey = "rate_limiter:" + key + ":" + previousWindowStart;

        // Increment the current window count in Redis
        long currentCount = jedis.incr(currentKey);
        jedis.expire(currentKey, 2 * windowSizeInSeconds);  // cleanup after 2 windows

        // Get previous window count
        String prevCountStr = jedis.get(prevKey);
        long prevCount = prevCountStr != null ? Long.parseLong(prevCountStr) : 0;

        // Calculate time passed in current window
        double elapsedRatio = (currentTimeMillis / 1000.0 - currentWindowStart) / windowSizeInSeconds;

        // Weighted count
        double weightedCount = currentCount + (1.0 - elapsedRatio) * prevCount;

        return weightedCount <= maxRequests;
    }

}
