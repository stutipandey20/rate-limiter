package ratelimiter;

public class TokenBucketRateLimiter implements RateLimiter {
    private final int capacity;
    private final double refillRatePerSec;
    private double tokens;
    private long lastRefillTimestamp;

    public TokenBucketRateLimiter(int capacity, double refillRatePerSec) {
        this.capacity = capacity;
        this.refillRatePerSec = refillRatePerSec;
        this.tokens = capacity;
        this.lastRefillTimestamp = System.nanoTime();
    }

    // key not being used here, but don't want to make the class abstract so just pas
    public synchronized boolean allowRequest(String key) {
        refill();

        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.nanoTime();
        double secondsElapsed = (now - lastRefillTimestamp) / 1_000_000_000.0;
        double tokensToAdd = secondsElapsed * refillRatePerSec;

        if (tokensToAdd > 0) {
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTimestamp = now;
        }
    }
}
