package ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowCounterRateLimiter implements RateLimiter {
    private final int maxRequests;
    private final int windowSizeInSeconds;

    private static class WindowData {
        long windowStart;
        int count;

        WindowData(long windowStart) {
            this.windowStart = windowStart;
            this.count = 0;
        }
    }

    private final Map<String, WindowData> currentWindow = new ConcurrentHashMap<>();
    private final Map<String, WindowData> previousWindow = new ConcurrentHashMap<>();

    public SlidingWindowCounterRateLimiter(int maxRequests, int windowSizeInSeconds) {
        this.maxRequests = maxRequests;
        this.windowSizeInSeconds = windowSizeInSeconds;
    }

    @Override
    public synchronized boolean allowRequest(String key) {

        long currentTimeMillis = System.currentTimeMillis();
        long currentWindowStart = currentTimeMillis / 1000 / windowSizeInSeconds * windowSizeInSeconds;
        long previousWindowStart = currentWindowStart - windowSizeInSeconds;

        // Initialize windows if not exist
        currentWindow.putIfAbsent(key, new WindowData(currentWindowStart));
        previousWindow.putIfAbsent(key, new WindowData(previousWindowStart));

        // If new window has started, shift windows
        WindowData currWin = currentWindow.get(key);
        if (currWin.windowStart != currentWindowStart) {
            previousWindow.put(key, currWin);
            currWin = new WindowData(currentWindowStart);
            currentWindow.put(key, currWin);
        }

        WindowData prevWin = previousWindow.get(key);

        // Calculate how far into the current window we are (e.g., 0.25 means 25% of
        // window passed)
        double elapsedFraction = (currentTimeMillis / 1000.0 - currentWindowStart) / windowSizeInSeconds;
        double weightedCount = currWin.count + (1.0 - elapsedFraction) * prevWin.count;

        if (weightedCount < maxRequests) {
            currWin.count++;
            return true;
        }

        return false;
    }
}
