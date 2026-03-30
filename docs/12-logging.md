# Chapter 12: JSON Logging with Logback & Logstash

**[← Chapter 11: Garbage Collection](11-garbage-collection.md)** | **[← Back to Table of Contents](../README.md)**

---

## Why Not `System.out.println`?

`System.out.println` works for quick debugging but is not suitable for production:

- No timestamps or log levels
- No way to filter by severity (DEBUG vs ERROR)
- No structured format — log aggregators (Splunk, ELK, Datadog) can't parse it
- No context (which request? which user? which thread?)

**Structured (JSON) logging** solves all of these. Every log entry is a JSON object that log aggregators can index, search, and alert on.

---

## The Logging Stack

| Component | Role |
|---|---|
| **SLF4J** | Logging **facade** (API) — your code only calls this |
| **Logback** | Logging **implementation** — does the actual logging |
| **Logstash Logback Encoder** | Formats log output as **JSON** |

This separation means you can swap Logback for another implementation without touching your application code.

---

## Dependencies

In `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("org.slf4j:slf4j-api:2.0.13")
}
```

`logback-classic` includes both Logback and bridges SLF4J automatically — no extra wiring needed.

---

## Basic Usage

```java
// src/main/java/com/example/logging/OrderService.java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {

    // One logger per class — use the class as the logger name
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public void processOrder(int orderId, String user) {
        log.info("Processing order {} for user {}", orderId, user);

        try {
            // ... business logic ...
            log.debug("Order {} validated successfully", orderId);
        } catch (Exception e) {
            log.error("Failed to process order {}", orderId, e);
        }
    }
}
```

> ⚠️ **Watch Out — Use parameterized logging, NOT string concatenation**
>
> ```java
> // ❌ Bad — evaluates the string even if DEBUG is disabled
> log.debug("Processing: " + order.toDetailedString());
>
> // ✅ Good — the string is only built if DEBUG is enabled
> log.debug("Processing: {}", order.toDetailedString());
> ```

---

## Configuring JSON Output: `logback.xml`

Create `app/src/main/resources/logback.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

  <!-- JSON output to console — great for production/containers -->
  <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <!-- Include only the short class name, not full package -->
      <shortenedLoggerNameLength>36</shortenedLoggerNameLength>
    </encoder>
  </appender>

  <!-- Human-readable output for local development -->
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <!-- Use JSON in production, plain text locally -->
  <springProfile name="prod">
    <root level="INFO">
      <appender-ref ref="JSON_CONSOLE"/>
    </root>
  </springProfile>

  <springProfile name="!prod">
    <root level="DEBUG">
      <appender-ref ref="CONSOLE"/>
    </root>
  </springProfile>

  <!-- Without Spring: uncomment this and remove springProfile blocks -->
  <!--
  <root level="INFO">
    <appender-ref ref="JSON_CONSOLE"/>
  </root>
  -->

</configuration>
```

A single log statement produces JSON like this:

```json
{
  "@timestamp": "2024-05-15T10:23:45.678Z",
  "@version": "1",
  "message": "Processing order 42 for user alice",
  "logger_name": "com.example.OrderService",
  "thread_name": "main",
  "level": "INFO",
  "level_value": 20000
}
```

---

## Logging Levels — When to Use Each

| Level | Use for |
|---|---|
| `TRACE` | Very fine-grained, step-by-step flow (rarely used in production) |
| `DEBUG` | Diagnostic info useful during development |
| `INFO` | Normal application events: "Server started", "Order processed" |
| `WARN` | Something unexpected happened but the app continues |
| `ERROR` | An operation failed; needs investigation |

Set the minimum level in `logback.xml`. Levels below the threshold are ignored (no performance cost after the initial `isEnabled` check).

---

## MDC — Adding Context to Every Log Line

**MDC (Mapped Diagnostic Context)** lets you attach key-value pairs to the current thread. Every subsequent log statement on that thread automatically includes those fields in JSON.

Perfect for attaching a request ID or user ID to every log line during an HTTP request:

```java
// src/main/java/com/example/logging/RequestFilter.java
import org.slf4j.MDC;
import java.util.UUID;

public class RequestFilter {

    public void handleRequest(String userId, Runnable handler) {
        // Set context at the start of the request
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("userId", userId);

        try {
            handler.run();
        } finally {
            MDC.clear();  // Always clear when done — threads are reused!
        }
    }
}
```

Now every log line during this request includes:

```json
{
  "@timestamp": "2024-05-15T10:23:45.678Z",
  "message": "Order validated",
  "level": "DEBUG",
  "requestId": "f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
  "userId": "alice"
}
```

This makes it trivial to filter all log lines for a single request in Kibana or Splunk.

> ⚠️ **Watch Out — Always `MDC.clear()` in a `finally` block**
>
> Thread pools reuse threads. If you forget to clear MDC, the next request on the same thread will inherit the previous request's context — leaking `userId` and `requestId` between requests.

---

## Adding Custom Fields to JSON

Use the Logstash `StructuredArguments` for one-off extra fields:

```java
import static net.logstash.logback.argument.StructuredArguments.*;

log.info("Payment processed",
    kv("orderId", 42),
    kv("amount", 99.95),
    kv("currency", "USD"));
```

JSON output:
```json
{
  "message": "Payment processed",
  "orderId": 42,
  "amount": 99.95,
  "currency": "USD"
}
```

---

## Security Reminder

> ⚠️ **Never log sensitive data**
>
> - ❌ Passwords, API keys, tokens
> - ❌ Credit card numbers
> - ❌ Personal data (email, SSN, full address) — check your GDPR obligations
>
> Audit your log output before deploying to production. Consider tools like [Logback's `MaskingMessageConverter`](https://github.com/lidalia-slf4j-ext/lidalia-slf4j-ext) if you need to mask patterns automatically.

---

## Key Takeaways

- Use **SLF4J** (`LoggerFactory.getLogger`) in your code — never import Logback directly
- Configure **Logstash Logback Encoder** to output JSON for production log aggregation
- Use **parameterized logging** (`{}`) instead of string concatenation for performance
- Use **MDC** to attach request-scoped context (requestId, userId) automatically to every log line
- Always **`MDC.clear()`** in a `finally` block to prevent context leaking across requests

---

**[← Chapter 11: Garbage Collection](11-garbage-collection.md)** | **[← Back to Table of Contents](../README.md)**
