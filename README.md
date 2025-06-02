# 🚦 Java Rate Limiter Project
Inspired from John Crickett Coding Challenges: Build Your Own Rate Limiter
Link: https://codingchallenges.fyi/challenges/challenge-rate-limiter/

This project implements multiple rate limiting algorithms in Java using the **Spark Java** framework. 
It supports both in-memory and Redis-backed rate limiters and simulates a scalable system that can run across multiple servers.

---

## 📌 Features

- ✅ Token Bucket
- ✅ Fixed Window Counter
- ✅ Sliding Window Log
- ✅ Sliding Window Counter
- ✅ Distributed Sliding Window Counter using Redis

---

## 🛠️ Tech Stack

- Java 17+
- Spark Java
- Redis (for distributed rate limiting)
- Postman (for testing)
- Docker (optional: for running Redis or servers on multiple ports) (not used yet)

---

## 🧠 Algorithms Implemented

### 1. **Token Bucket**
- Allows tokens to be refilled over time.
- Bursty traffic supported.
- Class: `TokenBucketRateLimiter`

### 2. **Fixed Window Counter**
- Counts requests in fixed intervals.
- Simple but suffers from boundary effects.
- Class: `FixedWindowRateLimiter`

### 3. **Sliding Window Log**
- Maintains timestamps of requests in a log.
- Accurate but memory-intensive.
- Class: `SlidingWindowLogRateLimiter`

### 4. **Sliding Window Counter**
- Hybrid of Fixed + Sliding Log.
- Weighted calculation from current and previous window.
- Class: `SlidingWindowCounterRateLimiter`

### 5. **Distributed Sliding Window Counter (Redis)**
- Redis-backed counter across multiple servers.
- Ensures consistency in a distributed environment.
- Class: `RedisSlidingWindowRateLimiter`

---

## 🚀 How to Run

### Run a Server

```bash
# Compile
javac -cp "spark-core-2.9.3.jar:jedis-4.4.3.jar" *.java

# Run on port 8080
java -cp ".:spark-core-2.9.3.jar:jedis-4.4.3.jar" ApiServer 8080

# Run on port 8081 (in a new terminal)
java -cp ".:spark-core-2.9.3.jar:jedis-4.4.3.jar" ApiServer 8081

or run via mvn command
example: mvn compile exec:java -Dexec.mainClass="main.java.ratelimiter.ApiServer"

🧪 How to Test (Postman)

Setup Requests
Create a new request:
GET http://localhost:{{serverBaseUrl}}/limited-redis

Add a Pre-request Script:

    let currentPort = pm.environment.get("currentPort") || "8080";
    let nextPort = currentPort === "8080" ? "8081" : "8080";
    pm.environment.set("serverBaseUrl", nextPort);
    pm.environment.set("currentPort", nextPort);

Use Collection Runner:
Run 60–100 iterations
Set delay: 0 ms (burst mode)

| Endpoint                      | Algorithm                | Example                            |
| ------------------            | ------------------------ | ---------------------------------- |
| `/unlimited`                  | N/A                      | `GET /unlimited`                   |
| `/limited-token`              | Token Bucket             | `GET /limited-token`               |
| `/limited-fixed`              | Fixed Window             | `GET /limited-fixed`               |
| `/limited-sliding-log`        | Sliding Log              | `GET /limited-sliding-log`         |
| `/limited-sliding-w-counter`  | Sliding Counter          | `GET /limited-sliding-w-counter`   |
| `/limited-redis`              | Sliding Counter w/ Redis | `GET /limited-redis`               |


📈 Future Improvements
Add JWT-based identity for per-user limiting.
Add Logging: Log incoming requests per port to verify distribution.
Integrate with Kubernetes for distributed testing.
Add monitoring (Prometheus + Grafana - Hook in Grafana or simple charts to visualize request patterns or blocked requests.) 
Unit Tests - Add JUnit tests for Redis rate limiter logic in isolation.

Thanks to John Crickett for creating this project guidelines.
Find more @https://codingchallenges.fyi/challenges/intro/

