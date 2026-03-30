# Chapter 9: Try-with-Resources & Exception Handling

**[← Chapter 8: Key Java 17–21 Features](08-java17-21-features.md)** | **[Chapter 10: The New HTTP Client →](10-http-client.md)**

---

## The Resource Leak Problem

Whenever you open a file, database connection, or network socket, you must **close it** when you're done. Forgetting is a classic bug that causes file handles to leak and connections to be exhausted.

The old pattern used `try/finally`:

```java
// Old way — verbose and easy to get wrong
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader("data.txt"));
    String line = reader.readLine();
    System.out.println(line);
} finally {
    if (reader != null) {
        try {
            reader.close();  // close() itself might throw!
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

That's a lot of noise for a simple file read.

---

## Try-with-Resources (Java 7+)

Any object implementing `AutoCloseable` can be used in a `try-with-resources` statement. The resource is **automatically closed** when the block exits — whether normally or via an exception:

```java
try (var reader = new BufferedReader(new FileReader("data.txt"))) {
    String line = reader.readLine();
    System.out.println(line);
}
// reader.close() is called automatically here
```

### Multiple Resources

List them separated by semicolons — they're closed in **reverse order** of declaration:

```java
try (var conn   = dataSource.getConnection();
     var stmt   = conn.prepareStatement("SELECT * FROM users");
     var result = stmt.executeQuery()) {

    while (result.next()) {
        System.out.println(result.getString("name"));
    }
}
// result closed first, then stmt, then conn
```

### Suppressed Exceptions

If both the body **and** `close()` throw, Java attaches the `close()` exception as a **suppressed exception** on the primary one:

```java
try {
    throw new RuntimeException("body error");
} finally {
    throw new RuntimeException("close error"); // In old code, "body error" is LOST!
}

// With try-with-resources, "close error" is suppressed — not lost:
Throwable[] suppressed = primaryException.getSuppressed();
```

---

## Writing Your Own `AutoCloseable`

Implement `AutoCloseable` to make your own objects work with try-with-resources:

```java
// src/main/java/com/example/exceptions/ManagedConnection.java
public class ManagedConnection implements AutoCloseable {
    private final String url;

    public ManagedConnection(String url) {
        this.url = url;
        System.out.println("Opened connection to " + url);
    }

    public String query(String sql) {
        return "result of: " + sql;
    }

    @Override
    public void close() {
        System.out.println("Closed connection to " + url);
    }
}

// Usage:
try (var conn = new ManagedConnection("db://localhost/mydb")) {
    System.out.println(conn.query("SELECT 1"));
}
// Output:
// Opened connection to db://localhost/mydb
// result of: SELECT 1
// Closed connection to db://localhost/mydb
```

---

## Multi-Catch (Java 7+)

Catch multiple exception types in one `catch` block:

```java
try {
    // could throw IOException or SQLException
    processFile();
} catch (IOException | SQLException e) {
    // e is effectively final here
    log.error("Data access failed", e);
}
```

---

## Custom Exceptions

Create application-specific exceptions to add context and make error handling explicit:

```java
// Checked exception — caller MUST handle or declare it
public class InsufficientFundsException extends Exception {
    private final double amount;

    public InsufficientFundsException(double amount) {
        super("Insufficient funds: needed %.2f more".formatted(amount));
        this.amount = amount;
    }

    public double getAmount() { return amount; }
}

// Unchecked exception — optional to catch
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(int userId) {
        super("User not found: " + userId);
    }
}
```

> 📝 **Checked vs. Unchecked — The Debate**
>
> - **Checked** (`extends Exception`): The compiler forces callers to handle it. Good for recoverable conditions (file not found, network timeout).
> - **Unchecked** (`extends RuntimeException`): No forced handling. Good for programming errors (invalid arguments, unexpected state).
>
> Modern Java style leans toward unchecked exceptions for most cases. Spring, Hibernate, and most frameworks use unchecked. Checked exceptions can cause verbose code and "exception pollution" in APIs.

---

## Exception Best Practices

```java
// ✅ Be specific — catch the most specific type
try { ... } catch (FileNotFoundException e) { ... }

// ❌ Too broad — hides bugs, swallows unexpected errors
try { ... } catch (Exception e) { ... }

// ❌ Never do this — silently swallows exceptions
try { ... } catch (Exception e) { /* nothing */ }

// ✅ Wrap and rethrow with context
try {
    repo.save(user);
} catch (DataAccessException e) {
    throw new ServiceException("Failed to save user " + user.id(), e);
}

// ✅ Use try-with-resources for anything that implements AutoCloseable
try (var stream = Files.lines(path)) {
    stream.forEach(System.out::println);
}
```

> 💡 **Tip — Include cause when wrapping exceptions**
>
> Always pass the original exception as the cause: `new MyException("message", cause)`. Without it, stack traces lose context and debugging becomes painful.

---

## Key Takeaways

- **Try-with-resources** guarantees `close()` is called — always use it for `AutoCloseable` resources
- Multiple resources are closed in reverse order
- Suppressed exceptions preserve the original error even when `close()` throws
- Prefer **unchecked exceptions** (`RuntimeException`) for most application code
- Always include the original exception as a **cause** when wrapping

---

**[← Chapter 8: Key Java 17–21 Features](08-java17-21-features.md)** | **[Chapter 10: The New HTTP Client →](10-http-client.md)**
