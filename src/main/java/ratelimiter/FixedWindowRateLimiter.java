package ratelimiter;

import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowRateLimiter implements RateLimiter {
    private final int windowSizeInSeconds;
    private final int maxRequests;
    private final ConcurrentHashMap<String, Window> userWindows = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int windowSize, int maxReq) {
        windowSizeInSeconds = windowSize;
        maxRequests = maxReq;
    }

    private static class Window {
        long windowStart;
        int requestCount;

        Window(long start) {
            windowStart = start;
            requestCount = 0;
        }
    }

    @Override
    public boolean allowRequest(String key) {
        long currentWindow = System.currentTimeMillis() / 1000 / windowSizeInSeconds;

        userWindows.compute(key, (k, window) -> {
            if (window == null || window.windowStart != currentWindow) {
                window = new Window(currentWindow);
            }
            if (window.requestCount < maxRequests) {
                window.requestCount++;
            } 
            return window;
        });
        return userWindows.get(key).requestCount <= maxRequests;
    }

}
