# Chapter 10: The New HTTP Client

**[← Chapter 9: Exceptions & Try-with-Resources](09-exceptions.md)** | **[Chapter 11: Garbage Collection →](11-garbage-collection.md)**

---

## Why a New HTTP Client?

Before Java 11, making an HTTP request in pure Java meant wrestling with `HttpURLConnection` — a verbose, callback-free API designed in the early 2000s. Most developers reached for Apache HttpClient or OkHttp instead.

```java
// Old way — HttpURLConnection (still works, but painful)
URL url = new URL("https://api.example.com/users");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
int code = conn.getResponseCode();
try (var br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
    String body = br.lines().collect(Collectors.joining("\n"));
}
conn.disconnect();
```

Java 11 shipped `java.net.http.HttpClient` — modern, fluent, and built in. No extra dependency needed.

---

## The Three Core Classes

| Class | Purpose |
|---|---|
| `HttpClient` | The "browser" — manages connections, redirects, HTTP version |
| `HttpRequest` | Describes what to request: method, URI, headers, body |
| `HttpResponse<T>` | The response — status code, headers, body |

---

## GET Request

```java
// src/main/java/com/example/http/HttpExample.java
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;

public class HttpExample {

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://jsonplaceholder.typicode.com/todos/1"))
            .header("Accept", "application/json")
            .GET()  // optional — GET is the default
            .build();

        HttpResponse<String> response =
            client.send(request, BodyHandlers.ofString());

        System.out.println("Status: " + response.statusCode());
        System.out.println("Body:   " + response.body());
    }
}
```

Output:
```
Status: 200
Body:   {
  "userId": 1,
  "id": 1,
  "title": "delectus aut autem",
  "completed": false
}
```

---

## POST Request with JSON Body

```java
String requestBody = """
        {
            "title": "foo",
            "body": "bar",
            "userId": 1
        }
        """;

HttpRequest postRequest = HttpRequest.newBuilder()
    .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
    .build();

HttpResponse<String> postResponse =
    client.send(postRequest, BodyHandlers.ofString());

System.out.println(postResponse.statusCode()); // 201
System.out.println(postResponse.body());
```

---

## Setting Timeouts and Redirect Policies

```java
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .followRedirects(HttpClient.Redirect.NORMAL)  // follow redirects
    .version(HttpClient.Version.HTTP_2)           // prefer HTTP/2
    .build();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/data"))
    .timeout(Duration.ofSeconds(30))  // per-request timeout
    .build();
```

> 💡 **Tip — Reuse the `HttpClient`**
>
> `HttpClient` manages a connection pool internally. Create **one instance** per application (or DI container), not one per request. Creating it per request is wasteful and slow.

---

## Async Requests with `CompletableFuture`

`sendAsync()` returns a `CompletableFuture` — the request runs in the background without blocking your thread:

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://jsonplaceholder.typicode.com/users"))
    .build();

CompletableFuture<HttpResponse<String>> future =
    client.sendAsync(request, BodyHandlers.ofString());

// Chain non-blocking operations
future
    .thenApply(HttpResponse::body)
    .thenAccept(body -> System.out.println("Got response: " + body.length() + " chars"))
    .exceptionally(ex -> { System.err.println("Failed: " + ex.getMessage()); return null; });

// Wait for completion (blocks current thread)
future.join();
```

### Firing Multiple Requests in Parallel

```java
List<String> urls = List.of(
    "https://jsonplaceholder.typicode.com/todos/1",
    "https://jsonplaceholder.typicode.com/todos/2",
    "https://jsonplaceholder.typicode.com/todos/3"
);

List<CompletableFuture<String>> futures = urls.stream()
    .map(url -> client
        .sendAsync(
            HttpRequest.newBuilder().uri(URI.create(url)).build(),
            BodyHandlers.ofString())
        .thenApply(HttpResponse::body))
    .toList();

// Wait for all and collect results
List<String> results = futures.stream()
    .map(CompletableFuture::join)
    .toList();
```

---

## Deserializing JSON with Jackson

Add Jackson to `build.gradle.kts`:

```kotlin
implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
```

```java
// src/main/java/com/example/http/Todo.java
public record Todo(int userId, int id, String title, boolean completed) {}
```

```java
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper mapper = new ObjectMapper();

HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

Todo todo = mapper.readValue(response.body(), Todo.class);
System.out.println(todo.title());    // "delectus aut autem"
System.out.println(todo.completed()); // false
```

> 💡 **Tip** — Create one `ObjectMapper` instance and reuse it. It's thread-safe and expensive to create.

---

## Available Body Handlers

```java
BodyHandlers.ofString()          // body as String
BodyHandlers.ofBytes()           // body as byte[]
BodyHandlers.ofFile(Path.of("output.json"))  // stream to a file
BodyHandlers.discarding()        // ignore body (status-only checks)
BodyHandlers.ofInputStream()     // stream for large responses
```

---

## Key Takeaways

- `java.net.http.HttpClient` (Java 11+) replaces the painful `HttpURLConnection`
- Use `HttpClient.newBuilder()` to configure timeouts, redirects, and HTTP version
- `sendAsync()` returns a `CompletableFuture` for non-blocking parallel requests
- **Reuse** `HttpClient` — it manages a connection pool
- Combine with Jackson's `ObjectMapper` to deserialize JSON responses directly into Records or POJOs

---

**[← Chapter 9: Exceptions & Try-with-Resources](09-exceptions.md)** | **[Chapter 11: Garbage Collection →](11-garbage-collection.md)**
