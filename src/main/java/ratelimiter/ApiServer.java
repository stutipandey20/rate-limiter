package ratelimiter;

import static spark.Spark.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApiServer {
    private static final ConcurrentHashMap<String, TokenBucketRateLimiter> tokenBucketLimiters = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, FixedWindowRateLimiter> fixedBucketLimiters = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, SlidingWindowLogRateLimiter> slidingWindowLimiters = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, RateLimiter> slidingCounterLimiters = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, RedisSlidingWindowRateLimiter> redisLimiter = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        final int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }

        port(port); // Spark method to set port

        get("/unlimited", (req, res) -> {
            return "Unlimited! Let's Go!!";
        });

        get("/limited-token", (req, res) -> {
            String ip = req.ip();

            RateLimiter limiter = tokenBucketLimiters.computeIfAbsent(ip, k -> new TokenBucketRateLimiter(10, 1.0));

            if (limiter != null && limiter.allowRequest(ip)) {
                return "Token Bucket: Request allowed! Do not over use.";
            } else {
                res.status(429);
                return "Token Bucket: Too Many Requests - Rate limit exceeded.";
            }
        });

        get("/limited-fixed", (req, res) -> {
            String ip = req.ip();

            fixedBucketLimiters.putIfAbsent(ip, new FixedWindowRateLimiter(60, 60));
            RateLimiter limiter = fixedBucketLimiters.get(ip);

            if (limiter.allowRequest(ip)) {
                return "Fixed Window: Request allowed!";
            } else {
                res.status(429);
                return "Fixed Window: Too Many Requests!";
            }

        });

        get("/limited-sliding-log", (req, res) -> {
            String ip = req.ip();

            RateLimiter limiter = slidingWindowLimiters.computeIfAbsent(ip,
                    k -> new SlidingWindowLogRateLimiter(60, 60)); // 60 reqs / 60 sec

            if (limiter.allowRequest(ip)) {
                return "Sliding window: Request allowed.";
            } else {
                res.status(429);
                return "Sliding window: Rate limit exceeded.";
            }
        });

        get("/limited-sliding-w-counter", (req, res) -> {
            String ip = req.ip();
            RateLimiter limiter = slidingCounterLimiters.computeIfAbsent(ip,
                    k -> new SlidingWindowCounterRateLimiter(60, 60));

            if (limiter.allowRequest(ip)) {
                return "Sliding counter: Request allowed.";
            } else {
                res.status(429);
                return "Sliding counter: Rate limit exceeded.";
            }
        });

        get("/limited-redis", (req, res) -> {
            String ip = req.ip();
            RateLimiter limiter = redisLimiter.computeIfAbsent(ip, k -> new RedisSlidingWindowRateLimiter(60, 60)); // 60
                                                                                                                    // req/min

            System.out.println("Server running on port: " + port);

            if (limiter.allowRequest(ip)) {
                return "Sliding counter: Request allowed. Server running on port: " + port;
            } else {
                res.status(429);
                return "Sliding counter: Rate limit exceeded. Server running on port: " + port;
            }
        });
    }
}
