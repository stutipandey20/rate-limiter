package ratelimiter;

import static spark.Spark.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApiServer {
    private static final ConcurrentHashMap<String, TokenBucketRateLimiter> tokenBucketLimiters = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, FixedWindowRateLimiter> fixedBucketLimiters = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        port(8080);

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
                return "Too Many Requests - Token Bucket! Rate limit exceeded. Please try again later.";
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
                return "Too Many Requests - Fixed Window!";
            }
            
        });
    }
}
