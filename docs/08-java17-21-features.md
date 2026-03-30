# Chapter 8: Key Java 17–25 Features

**[← Chapter 7: Enums & Sealed Classes](07-enums-sealed.md)** | **[Chapter 9: Exceptions & Try-with-Resources →](09-exceptions.md)**

---

## Overview

Java releases a new version every 6 months. This chapter is a focused tour of the most impactful language and library additions from Java 17 through Java 25 (the current LTS) that aren't covered in their own dedicated chapters. Features are grouped by theme and labelled with the version where they became **final** (stable for production).

---

## Text Blocks (Java 15 — Final)

Multi-line strings drop all the escape noise:

```java
// Old way
String json = "{\n    \"name\": \"Alice\",\n    \"age\": 30\n}";

// Text block
String json = """
        {
            "name": "Alice",
            "age": 30
        }
        """;
```

The closing `"""` sets the left margin — everything to its left is stripped. Combine with `.formatted()`:

```java
String body = """
        Hello, %s! Your score is %d.
        """.formatted(name, score);
```

---

## `instanceof` Pattern Matching (Java 16 — Final)

Eliminates the redundant cast after an `instanceof` check:

```java
// Before
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}

// After
if (obj instanceof String s && s.length() > 5) {
    System.out.println(s.toUpperCase());
}
```

---

## Record Patterns (Java 21 — Final, JEP 440)

Pattern matching now works inside records, enabling **deconstruction** directly in `instanceof` and `switch`:

```java
record Point(int x, int y) {}
record Line(Point start, Point end) {}

// Deconstruct a record in instanceof
if (obj instanceof Point(int x, int y)) {
    System.out.println("x=" + x + ", y=" + y);
}

// Nested deconstruction in switch
String describe(Object shape) {
    return switch (shape) {
        case Circle(double r)               -> "circle r=%.1f".formatted(r);
        case Rectangle(double w, double h)  -> "rect %.1fx%.1f".formatted(w, h);
        case Line(Point(int x1, int y1),
                  Point(int x2, int y2))    -> "line (%d,%d)→(%d,%d)".formatted(x1, y1, x2, y2);
        default                             -> "unknown";
    };
}
```

> 💡 **Tip** — Record patterns combine naturally with sealed classes from Chapter 7. The compiler can verify exhaustiveness of the switch when the type is sealed.

---

## Enhanced NullPointerExceptions (Java 14 — Final)

Since Java 14, the JVM pinpoints exactly which variable was null:

```
// Before
NullPointerException at com.example.App.main(App.java:10)

// After (Java 14+)
Cannot invoke "String.length()" because "user.address" is null
```

Enabled by default since Java 14.

---

## `var` — Local Variable Type Inference (Java 10 — Final)

```java
var list = new ArrayList<String>();        // inferred: ArrayList<String>
var map  = new HashMap<String, Integer>(); // inferred: HashMap<String, Integer>

for (var entry : map.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}
```

Use when the type is obvious from the right-hand side. Do **not** use for fields, return types, or when the type is unclear (`var x = compute()`).

> ⚠️ **Watch Out** — `var` is compile-time type inference, not dynamic typing. Java remains statically typed.

---

## `String` Improvements (Java 11–15)

```java
"  hello  ".strip()         // "hello" — Unicode-aware trim (Java 11+)
"   ".isBlank()             // true (Java 11+)
"a\nb\nc".lines()           // Stream<String>: ["a","b","c"] (Java 11+)
"ha".repeat(3)              // "hahaha" (Java 11+)
"Value: %d".formatted(42)   // "Value: 42" — instance method of String.format (Java 15+)
```

---

## Unnamed Variables & Patterns (Java 22 — Final, JEP 456)

Use `_` when you need to receive a variable but don't care about its value:

```java
// Unnamed local variable — suppress "unused variable" warnings
try {
    int result = Integer.parseInt(s);
} catch (NumberFormatException _) {   // don't need the exception object
    System.out.println("Not a number");
}

// Unnamed pattern variable in instanceof
if (obj instanceof String _) {       // just checking the type, not binding
    count++;
}

// Unnamed pattern in switch
switch (shape) {
    case Circle _    -> System.out.println("a circle");
    case Rectangle _ -> System.out.println("a rectangle");
}

// Unnamed variable in for-each (when index is unused)
for (var _ : collection) {
    total++;
}
```

> 💡 **Tip** — `_` signals intent: "I know this value exists but I don't need it." This is cleaner than naming variables `ignored` or `unused`.

---

## Sequenced Collections (Java 21 — Final, JEP 431)

A uniform API for getting the first and last element of ordered collections:

```java
SequencedCollection<String> list = new ArrayList<>(List.of("a", "b", "c"));
list.getFirst();     // "a"
list.getLast();      // "c"
list.addFirst("z");
list.removeLast();
list.reversed();     // reversed view (no copy)

SequencedMap<String, Integer> map = new LinkedHashMap<>();
map.put("one", 1); map.put("two", 2);
map.firstEntry();    // Map.Entry<"one", 1>
map.lastEntry();     // Map.Entry<"two", 2>
map.reversed();
```

---

## Virtual Threads (Java 21 — Final, JEP 444)

Lightweight JVM-managed threads — you can create millions without exhausting memory:

```java
// Simple virtual thread
Thread.ofVirtual().start(() ->
    System.out.println("Running on virtual thread"));

// For servers: one virtual thread per task
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 10_000).forEach(i ->
        executor.submit(() -> handleRequest(i)));
}
```

Blocking I/O inside a virtual thread causes the JVM (not the OS) to suspend it, freeing the carrier thread for other work. Great for I/O-bound high-concurrency code. Not a replacement for parallel streams when you need CPU-bound parallelism.

> ⚠️ **Watch Out — Thread Pinning** — Avoid holding `synchronized` locks while blocking inside a virtual thread. Use `ReentrantLock` instead.

---

## Stream Gatherers (Java 25 — Final, JEP 485)

The first new intermediate Stream operation since Java 8. Gatherers let you write **stateful, user-defined intermediate operations**:

```java
import java.util.stream.Gatherers;

// windowFixed — chunk stream into fixed-size windows
Stream.of(1, 2, 3, 4, 5, 6)
    .gather(Gatherers.windowFixed(2))
    .toList();
// [[1,2], [3,4], [5,6]]

// windowSliding — overlapping windows
IntStream.rangeClosed(1, 5).boxed()
    .gather(Gatherers.windowSliding(3))
    .toList();
// [[1,2,3], [2,3,4], [3,4,5]]

// fold — running accumulation (emits every step)
Stream.of(1, 2, 3, 4, 5)
    .gather(Gatherers.fold(() -> 0, Integer::sum))
    .toList();
// [1, 3, 6, 10, 15]

// mapConcurrent — parallel map with concurrency limit (great with virtual threads)
urls.stream()
    .gather(Gatherers.mapConcurrent(8, url -> fetch(url)))
    .toList();
```

Write your own gatherer with `Gatherer.ofSequential()` or `Gatherer.of()` for parallel support.

---

## Primitive Types in Patterns (Java 25 — Final, JEP 488)

Pattern matching now works with **primitive types**, unlocking them in `instanceof` and `switch`:

```java
// instanceof with primitive
Object obj = 42;
if (obj instanceof int i) {
    System.out.println("int: " + i);
}

// switch with primitive type patterns
static String describe(long val) {
    return switch (val) {
        case int i when i < 0   -> "negative int";
        case int i              -> "non-negative int";
        case long l when l < 0  -> "negative long";
        case long l             -> "large long: " + l;
    };
}
```

Previously, primitives had to be boxed (`Integer`, `Long`) to participate in patterns. Now they're first-class.

---

## Flexible Constructor Bodies (Java 25 — Final, JEP 492)

Before Java 25, constructors had a strict rule: if you call `super()` or `this()`, it **must be the very first statement**. This blocked many useful patterns:

```java
// Before Java 25 — illegal!
class PositiveInt {
    final int value;
    PositiveInt(int v) {
        if (v <= 0) throw new IllegalArgumentException(); // ERROR: super() must come first
        super();
        this.value = v;
    }
}
```

Java 25 allows statements **before** the `super()` / `this()` call, as long as they don't access `this`:

```java
// Java 25+
class PositiveInt {
    final int value;
    PositiveInt(int v) {
        if (v <= 0) throw new IllegalArgumentException("Must be positive: " + v);
        super();  // can now come after validation
        this.value = v;
    }
}

// Useful for computing arguments passed to super()
class Circle extends Shape {
    Circle(int diameter) {
        var radius = diameter / 2.0;  // pre-process before super()
        super(radius);
    }
}
```

> 💡 **Tip** — This is especially useful for validating arguments before they reach the parent constructor.

---

## Module Import Declarations (Java 25 — Final, JEP 494)

Import an entire Java module's public API with a single statement instead of listing dozens of packages:

```java
// Before — many individual imports
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Path;
// ...

// Java 25 — import the whole module
import module java.base;  // imports all exported packages from java.base

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of("data.txt"));
        Map<Integer, List<String>> grouped = lines.stream()
            .collect(Collectors.groupingBy(String::length));
    }
}
```

> 📝 **Note** — Module imports don't make your project a JPMS module. They're just a convenience shorthand for imports. Single-type imports still take precedence over module imports if names conflict.

---

## Structured Concurrency (Java 25 — Final, JEP 505)

Treat multiple concurrent tasks as a single unit of work with automatic cancellation and clean error handling:

```java
import java.util.concurrent.StructuredTaskScope;

record UserDetails(User user, List<Order> orders) {}

UserDetails fetchUserDetails(int userId) throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        var userTask   = scope.fork(() -> userRepo.findById(userId));
        var ordersTask = scope.fork(() -> orderRepo.findByUser(userId));

        scope.join().throwIfFailed();  // waits for both; cancels either if one fails

        return new UserDetails(userTask.get(), ordersTask.get());
    }
}
```

**`ShutdownOnFailure`** — if any task fails, all others are cancelled automatically.  
**`ShutdownOnSuccess`** — completes when the **first** task succeeds (race pattern).

Compare to `CompletableFuture.allOf()` — no manual cancellation, no chained `.thenApply()`, exceptions propagate naturally.

---

## Scoped Values (Java 25 — Final, JEP 506)

Pass read-only context (request ID, user, security principal) through a deep call stack without threading it through every method signature:

```java
// Declare once (class-level constant)
private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

// Bind for the duration of a request
ScopedValue.where(REQUEST_ID, "req-abc-123").run(() -> {
    processRequest();   // and everything it calls...
});

// Anywhere down the call stack, read it
void processRequest() {
    log.info("Handling {}", REQUEST_ID.get());  // "req-abc-123"
    callDatabase();   // also gets REQUEST_ID.get() transparently
}
```

**Scoped Values vs `ThreadLocal`:**

| | `ThreadLocal` | Scoped Value |
|---|---|---|
| Mutable | ✅ | ❌ Immutable after binding |
| Cleanup required | ✅ `remove()` call | ❌ Automatic when scope exits |
| Virtual thread support | ⚠️ Pinning risk | ✅ Designed for virtual threads |
| Inheritance to child threads | Manual | ✅ Automatic |

---

## Ahead-of-Time Class Loading (Java 24, JEP 483)

The JVM can pre-load and link classes at build time, putting them into a cache. On subsequent runs, the startup phase is skipped — the JVM jumps straight to running your code:

```bash
# Training run — JVM records which classes are loaded
java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -jar app.jar

# Build the AOT cache
java -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot -jar app.jar

# Production run — uses the pre-built cache
java -XX:AOTCache=app.aot -jar app.jar
```

Benefits: faster startup, lower time-to-first-request — ideal for serverless and CLI tools.

> 📝 **Note** — This is different from GraalVM Native Image. AOT class loading is standard JVM, not compiled-to-native.

---

## Class-File API (Java 24 — Final, JEP 484)

A standard JDK API for reading, writing, and transforming `.class` files programmatically — replacing third-party ASM/Javassist usage:

```java
import java.lang.classfile.*;

// Read a class file
ClassFile cf = ClassFile.of();
ClassModel model = cf.parse(Path.of("MyClass.class"));

model.methods().forEach(method ->
    System.out.println(method.methodName() + " → " + method.methodTypeSymbol()));

// Transform: strip all @Deprecated methods from a class
byte[] transformed = cf.transform(model,
    ClassTransform.dropping(element ->
        element instanceof MethodModel m &&
        m.findAttribute(Attributes.deprecated()).isPresent()));
```

Mostly relevant for framework authors, bytecode agents, and tooling. As an application developer you'll rarely use this directly, but it's good to know it exists as the standard way to do classfile manipulation.

---

## Key Takeaways

| Feature | Java version | Impact |
|---|---|---|
| Text blocks | 15 | Cleaner multi-line strings |
| `instanceof` pattern matching | 16 | No redundant casts |
| Record patterns | 21 | Deconstruct records in switch/instanceof |
| Sequenced Collections | 21 | Uniform first/last API |
| Virtual Threads | 21 | Millions of concurrent tasks, simple blocking code |
| Unnamed variables `_` | 22 | Suppress unused variable noise |
| Stream Gatherers | 25 | User-defined windowing, folding, concurrent mapping |
| Primitive patterns | 25 | Pattern match on `int`, `long`, etc. |
| Flexible constructors | 25 | Pre-`super()` statements in constructors |
| Module imports | 25 | Single-line import of an entire module |
| Structured Concurrency | 25 | Composable, auto-cancelling concurrent tasks |
| Scoped Values | 25 | Thread-local context without the pitfalls |
| AOT Class Loading | 24 | Faster startup via pre-built class cache |

---

**[← Chapter 7: Enums & Sealed Classes](07-enums-sealed.md)** | **[Chapter 9: Exceptions & Try-with-Resources →](09-exceptions.md)**
