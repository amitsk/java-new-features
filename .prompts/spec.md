# Java New Features Tutorial — Spec

## Goal

Create a comprehensive, multi-chapter tutorial on modern Java (Java 17+) aimed at **CS students and beginner Java programmers**. The tutorial should be approachable but go beyond the basics, focusing on features introduced in newer Java versions. Each chapter should build naturally on the previous one, with clear navigation links.

---

## Audience

- **Primary**: CS students in their 2nd–3rd year who know Java basics (classes, loops, conditionals, basic OOP)
- **Secondary**: Developers coming from another language (Python, JavaScript) who are learning Java
- **Assumed knowledge**:
  - Basic Java syntax (variables, methods, classes, interfaces)
  - Basic understanding of object-oriented programming (inheritance, polymorphism)
  - Basic command-line usage
  - Some familiarity with build tools is helpful but not required
- **Not assumed**: Spring Boot, advanced design patterns, JVM internals, functional programming

### Tone & Style
- Conversational and encouraging — "Here's why this matters" over dry reference docs
- Every concept must include a runnable code example
- Compare new features against the "old way" (Java 8 era) so students appreciate the improvement
- Use real-world analogies and relatable scenarios
- Highlight common beginner mistakes as callout boxes: 💡 **Tip**, ⚠️ **Watch Out**, 📝 **Note**

---

## Structure

The tutorial is split into **chapters** with top-level navigation. Each chapter page should include:
- A brief intro explaining what the chapter covers and *why it matters*
- Prerequisite note (what the reader should know before reading)
- Code examples in self-contained snippets (no dependency on earlier chapters)
- A "Key Takeaways" summary at the end
- "Next Chapter →" and "← Previous Chapter" navigation links

---

## Chapter Outline

---

### Chapter 1: Introduction

**Goal**: Orient the reader, set expectations, and get them excited.

**Content**:
- Who this tutorial is for and what they'll learn
- Links to the official [Java Tutorials](https://docs.oracle.com/javase/tutorial/) and [OpenJDK](https://openjdk.org/) for background reading
- Brief history: Java's release cadence (LTS releases: Java 17, 21, etc.) and why staying current matters
- Overview of tools used throughout the tutorial:
  - JDK 21 (or latest LTS)
  - Gradle as the build tool
  - IntelliJ IDEA (Community Edition is free) or VS Code with Java extensions
- "What you'll be able to do after this tutorial" — motivate with outcomes:
  - Write cleaner, more expressive Java using Records, Sealed classes, Pattern Matching
  - Understand modern Java idioms used in real codebases
  - Write testable code with JUnit 5 and Mockito
  - Make HTTP calls and handle JSON
  - Understand how the JVM garbage collector works at a high level
- Mention that the tutorial expects basic Java (variables, loops, classes) and some knowledge of Gradle and Spring Boot concepts, but explains what it needs as it goes

---

### Chapter 2: Getting Started with Gradle

**Goal**: Get the reader a working Java 21 project using Gradle, demystify the build tool.

**Prerequisite note**: Familiarity with the command line; no prior Gradle knowledge needed.

**Content**:
- What is a build tool and why we need one (vs. compiling `.java` files manually)
- Installing the JDK (using SDKMAN! as the recommended approach on macOS/Linux, direct download on Windows)
- Gradle vs. Maven — brief explanation of why Gradle is increasingly preferred (Groovy/Kotlin DSL, speed, flexibility)
- Creating a new project:
  ```bash
  gradle init --type java-application --dsl kotlin
  ```
  Walk through every generated file:
  - `build.gradle.kts` — dependencies, plugins, Java version
  - `settings.gradle.kts` — project name
  - `src/main/java/` and `src/test/java/` — source structure
  - `.gitignore`, `gradlew` wrapper
- Setting the Java toolchain to Java 21:
  ```kotlin
  java {
      toolchain {
          languageVersion = JavaLanguageVersion.of(21)
      }
  }
  ```
- Running the application: `./gradlew run`
- Running tests: `./gradlew test`
- Common Gradle tasks (`clean`, `build`, `dependencies`)
- Adding a dependency (e.g., add Guava or Jackson) to `build.gradle.kts`
- 💡 **Tip**: Explain the Gradle wrapper (`gradlew`) — why it ensures everyone uses the same Gradle version
- ⚠️ **Watch Out**: JAVA_HOME and PATH gotchas — how to verify the right JDK is being used (`java -version`)

**Lead-in**: "Now that our project is set up, let's write some tests to make sure our code works as we learn new features."

---

### Chapter 3: Unit Testing with JUnit 5 (and Mockito + AssertJ)

**Goal**: Teach test-driven thinking from the start; cover the modern JUnit 5 ecosystem.

**Prerequisite note**: Basic knowledge of methods and classes.

**Content**:
- Why write tests? Brief motivation — catching bugs early, documenting behavior, enabling refactoring
- JUnit 5 vs. JUnit 4 differences (annotations changed, more expressive, modular)
- Add dependencies to `build.gradle.kts`:
  ```kotlin
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
  testImplementation("org.mockito:mockito-core:5.x")
  testImplementation("org.assertj:assertj-core:3.x")
  ```
- **Writing your first test** — a simple `Calculator` class with `add()`, `subtract()`, etc.
- **JUnit 5 Annotations**:
  - `@Test`, `@BeforeEach`, `@AfterEach`, `@BeforeAll`, `@AfterAll`
  - `@DisplayName` — human-readable test names shown in reports
  - `@Disabled` — skipping tests and why
  - `@Tag` — grouping tests (e.g., `@Tag("slow")`, `@Tag("integration")`)
  - Running specific tags with Gradle
- **Parameterized Tests**:
  - `@ParameterizedTest` with `@ValueSource`, `@CsvSource`, `@MethodSource`
  - Custom display names: `@ParameterizedTest(name = "{index}: {0} + {1} = {2}")`
  - Real example: testing a currency converter with multiple input/output pairs
- **AssertJ**:
  - Why AssertJ over plain `assertEquals` — fluent assertions are more readable
  - Common assertions: `assertThat(x).isEqualTo(y)`, `.contains()`, `.hasSize()`, `.isInstanceOf()`
  - Soft assertions: `assertSoftly(...)` — collect all failures instead of stopping at first
- **Mockito**:
  - What is a mock? Why mock? (Isolate the unit under test from external dependencies)
  - `@Mock` and `@InjectMocks` with `@ExtendWith(MockitoExtension.class)`
  - `when(...).thenReturn(...)` — stubbing
  - `verify(mock).method(...)` — verifying interactions
  - `ArgumentCaptor` — capturing arguments passed to mocks
  - Example: testing a `UserService` that calls a `UserRepository` mock
- **Excluding tests / Groups**:
  - How to configure Gradle to include/exclude tags:
    ```kotlin
    tasks.test {
        useJUnitPlatform {
            includeTags("fast")
            excludeTags("slow")
        }
    }
    ```
- 💡 **Tip**: Naming convention — `GivenX_WhenY_ThenZ` or `shouldDoSomethingWhen...`
- ⚠️ **Watch Out**: Don't mock types you don't own (e.g., `String`, `List`) — mock your own interfaces

**Lead-in**: "Great — we can write and test code. Now let's learn one of Java's most powerful abstraction tools: Generics."

---

### Chapter 4: Generics

**Goal**: Demystify generics — one of the most intimidating but important Java features for beginners.

**Prerequisite note**: Understanding of classes and interfaces.

**Content**:
- What problem do generics solve? (Type-safe containers, reusable algorithms without `Object` casts)
- Before generics (Java 1.4 era) vs. after — show a `ClassCastException` example
- Generic classes:
  ```java
  public class Box<T> {
      private T value;
      public Box(T value) { this.value = value; }
      public T getValue() { return value; }
  }
  ```
- Generic methods:
  ```java
  public <T extends Comparable<T>> T max(T a, T b) { ... }
  ```
- Bounded type parameters: `<T extends Comparable<T>>`, `<T extends Number>`
- Wildcards: `<?>`, `<? extends Foo>`, `<? super Foo>` — PECS mnemonic (Producer Extends, Consumer Super)
- Generics with interfaces — writing a generic `Repository<T, ID>` interface
- Type erasure — what it is and why it means you can't do `new T()` at runtime
- Practical example: a generic `Pair<A, B>` record (linking to the Records chapter)
- Common beginner mistakes: raw types, unchecked warnings

**Lead-in**: "Now that you understand type safety, let's explore the powerful Collection and Stream APIs that make Java code elegant and concise."

---

### Chapter 5: Collections, Streams, and Functional Programming

**Goal**: Cover the full modern collection and stream API, including functional paradigms.

**Prerequisite note**: Basic knowledge of loops and arrays.

**Content**:

#### 5.1 Collections Overview
- `List`, `Set`, `Map`, `Queue` — when to use each
- Factory methods (Java 9+): `List.of(...)`, `Set.of(...)`, `Map.of(...)`, `Map.entry(...)`
  - ⚠️ **Watch Out**: These are **immutable** — common beginner gotcha
- Mutable alternatives: `new ArrayList<>()`, `new HashMap<>()`, `new LinkedHashMap<>()`
- Choosing the right implementation: `ArrayList` vs `LinkedList`, `HashMap` vs `TreeMap` vs `LinkedHashMap`

#### 5.2 Looping and Iteration
- `for-each` loop (enhanced for)
- `Iterator` — when and why to use it (safe removal during iteration)
- `ListIterator` — bidirectional iteration
- `forEach` with a lambda: `list.forEach(item -> System.out.println(item))`
- Method references: `list.forEach(System.out::println)`

#### 5.3 Streams
- What is a Stream? (A pipeline for processing sequences of elements lazily)
- Creating streams: `collection.stream()`, `Stream.of()`, `IntStream.range()`, `Files.lines()`
- **Intermediate operations** (lazy, return a Stream):
  - `filter()`, `map()`, `flatMap()`, `distinct()`, `sorted()`, `limit()`, `skip()`, `peek()`
- **Terminal operations** (eager, trigger evaluation):
  - `collect()`, `forEach()`, `count()`, `findFirst()`, `findAny()`, `anyMatch()`, `allMatch()`, `noneMatch()`
  - Numeric: `sum()`, `average()`, `min()`, `max()`, `summaryStatistics()`
- **Collectors**: `Collectors.toList()`, `toSet()`, `toMap()`, `groupingBy()`, `joining()`, `partitioningBy()`
- `flatMap()` in depth — flattening nested structures
- **Optional**: return type of `findFirst()`, etc. — `isPresent()`, `orElse()`, `orElseThrow()`, `map()`

#### 5.4 Parallel Streams
- `collection.parallelStream()` or `.parallel()`
- When to use: CPU-bound operations on large datasets
- ⚠️ **Watch Out**: Thread safety, ordering, and overhead on small datasets — don't blindly parallelize
- `ForkJoinPool` under the hood (brief mention)

#### 5.5 Performance Tips
- Avoid boxing/unboxing — prefer `IntStream`, `LongStream`, `DoubleStream` for primitives
- Use lazy evaluation correctly — don't forget terminal operations
- Avoid stateful intermediate operations in parallel streams
- `Stream.of()` vs `Arrays.stream()` for arrays

**Lead-in**: "You've seen how Records appeared briefly — let's dive deep into Records and why they replace verbose POJOs."

---

### Chapter 6: Records (Java 16+)

**Goal**: Show how Records eliminate boilerplate, and when/when not to use them.

**Prerequisite note**: Understanding of classes and constructors.

**Content**:
- The problem with POJOs: `toString()`, `equals()`, `hashCode()`, getters — all boilerplate
- Lombok as a common workaround — why Records are better (built into the language, no annotation processing)
- Declaring a Record:
  ```java
  public record Point(int x, int y) {}
  ```
  What the compiler generates: constructor, accessors (`x()`, `y()`), `equals()`, `hashCode()`, `toString()`
- Adding validation in compact constructor:
  ```java
  public record Range(int min, int max) {
      public Range {
          if (min > max) throw new IllegalArgumentException("min must be <= max");
      }
  }
  ```
- Adding custom methods to Records
- Records as DTOs (Data Transfer Objects) — great for API responses
- Implementing interfaces with Records
- ⚠️ **Watch Out**: Records are **immutable** and **final** — can't extend another class, can't have mutable fields
- When NOT to use Records: when you need mutability, inheritance, or JPA entities (JPA requires no-arg constructor and mutable fields)
- Generic Records: `record Pair<A, B>(A first, B second) {}`

---

### Chapter 7: Enums and Sealed Classes (Java 17+)

**Goal**: Modern type-safe modeling of finite state and domain concepts.

**Prerequisite note**: Understanding of interfaces and inheritance.

**Content**:

#### 7.1 Enums in depth
- Basic enum declaration and usage
- Enums with fields, constructors, and methods
- Implementing an interface with an enum
- `EnumSet` and `EnumMap` — efficient enum collections
- `switch` on enum values

#### 7.2 Pattern Matching in `switch` (Java 21 — Standard)
- Old `switch` statement vs. new `switch` expression
- Pattern matching with `switch`:
  ```java
  String describe(Object obj) {
      return switch (obj) {
          case Integer i -> "Integer: " + i;
          case String s  -> "String: " + s;
          case null      -> "null";
          default        -> "other";
      };
  }
  ```
- Guarded patterns: `case Integer i when i > 0 -> "positive int"`

#### 7.3 Sealed Classes (Java 17+)
- What is a sealed class? Restricting which classes can extend it
  ```java
  public sealed interface Shape permits Circle, Rectangle, Triangle {}
  public record Circle(double radius) implements Shape {}
  public record Rectangle(double width, double height) implements Shape {}
  ```
- Sealed classes + `switch` — exhaustive pattern matching, no need for `default`
- Why this is better than raw inheritance: the compiler enforces completeness
- Use case: modeling domain errors, state machines, AST nodes
- `permits`, `final`, `sealed`, `non-sealed` keywords

---

### Chapter 8: Key Java 17–21 Features

**Goal**: A focused tour of language and API improvements introduced in Java 17–21.

**Prerequisite note**: None beyond basics.

**Content**:

#### 8.1 Text Blocks (Java 15 — Standard)
  ```java
  String json = """
      {
          "name": "Alice",
          "age": 30
      }
      """;
  ```
  - Indentation stripping rules
  - Comparison with string concatenation and `String.format()`

#### 8.2 `instanceof` Pattern Matching (Java 16+)
  ```java
  if (obj instanceof String s && s.length() > 5) {
      System.out.println(s.toUpperCase());
  }
  ```

#### 8.3 Enhanced `NullPointerException` Messages (Java 14+)
  - JVM now tells you *which* variable was null — show an example

#### 8.4 `Map` and `List` Improvements
  - `Map.copyOf()`, `List.copyOf()`, `Set.copyOf()`
  - `Map.entry()` for simple entry creation
  - `Map.getOrDefault()`, `Map.computeIfAbsent()`, `Map.merge()`

#### 8.5 `String` Improvements
  - `strip()` vs `trim()` (Unicode-aware)
  - `isBlank()`, `lines()`, `repeat()`, `formatted()`

#### 8.6 `var` — Local Variable Type Inference (Java 10+)
  - `var list = new ArrayList<String>();`
  - When to use and when it hurts readability
  - ⚠️ **Watch Out**: `var` only works for local variables, not fields or method parameters

#### 8.7 Virtual Threads (Java 21 — Project Loom)
  - The problem: platform threads are expensive (~1MB stack)
  - Virtual threads are lightweight (managed by JVM, not OS)
  - Creating a virtual thread:
    ```java
    Thread.ofVirtual().start(() -> System.out.println("Hello from virtual thread"));
    ```
  - `Executors.newVirtualThreadPerTaskExecutor()`
  - When they shine: I/O-bound tasks, high concurrency servers
  - ⚠️ **Watch Out**: Not a replacement for reactive programming in all cases; pinning issues

#### 8.8 Sequenced Collections (Java 21)
  - `SequencedCollection`, `SequencedMap` — `getFirst()`, `getLast()`, `reversed()`

---

### Chapter 9: Try-with-Resources and Exception Handling

**Goal**: Teach safe, clean resource management — a common source of bugs for beginners.

**Prerequisite note**: Basic understanding of exceptions.

**Content**:
- The resource leak problem: what happens when you forget to close a file/connection
- Old way: `try/finally` blocks — verbose and error-prone
- `try-with-resources` (Java 7+, but often missed by beginners):
  ```java
  try (var reader = new BufferedReader(new FileReader("file.txt"))) {
      System.out.println(reader.readLine());
  }
  ```
- How it works: the `AutoCloseable` interface and `close()` method
- Multiple resources in one `try` block
- Suppressed exceptions — what happens when both the body and `close()` throw
- Writing your own `AutoCloseable` class
- `multi-catch`: `catch (IOException | SQLException e)`
- Custom exceptions — when and how to create them
- Checked vs. unchecked exceptions — the ongoing debate and modern best practices
- 💡 **Tip**: Prefer specific exception types over catching `Exception` or `Throwable`

---

### Chapter 10: The New HTTP Client (Java 11+)

**Goal**: Make modern HTTP requests in pure Java without third-party libraries.

**Prerequisite note**: Basic understanding of networking (what HTTP is).

**Content**:
- The old way: `HttpURLConnection` — verbose and painful; `Apache HttpClient` as a common workaround
- Introducing `java.net.http.HttpClient` — built in since Java 11
- Making a GET request:
  ```java
  HttpClient client = HttpClient.newHttpClient();
  HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create("https://api.example.com/users"))
      .GET()
      .build();
  HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
  System.out.println(response.body());
  ```
- POST request with a JSON body
- Setting headers, timeouts, and redirect policies
- Async requests with `sendAsync()` and `CompletableFuture`
- Deserializing JSON responses using Jackson or Gson
- HTTP/2 support — mention it's automatic
- ⚠️ **Watch Out**: Always close or reuse `HttpClient` — it's a heavyweight object

---

### Chapter 11: Garbage Collection — ZGC and G1GC

**Goal**: Demystify the JVM GC — give students enough to understand behavior and tune for their app.

**Prerequisite note**: Basic understanding of memory (stack vs. heap concept).

**Content**:
- What is garbage collection? Why Java (and Go, etc.) use it vs. manual memory management (C/C++)
- How GC works at a high level: mark-and-sweep, generational hypothesis (most objects die young)
- The generations: Young (Eden, Survivor S0/S1), Old (Tenured), Metaspace
- **G1GC** (default since Java 9):
  - Region-based — avoids full-heap pauses
  - Aims for a configurable pause target: `-XX:MaxGCPauseMillis=200`
  - Good for most server applications
- **ZGC** (production-ready since Java 15, greatly improved in Java 21):
  - Sub-millisecond pause times — designed for huge heaps (terabytes)
  - Concurrent — does most work while the application runs
  - Enable with: `-XX:+UseZGC`
- **Shenandoah** — brief mention as another low-latency GC (Red Hat)
- Key JVM flags:
  ```
  -Xms512m           # Initial heap size
  -Xmx2g             # Max heap size
  -XX:+UseZGC        # Use ZGC
  -XX:+UseG1GC       # Use G1 (often default)
  -Xlog:gc*          # Verbose GC logging
  ```
- Reading GC logs — what to look for (pause times, heap usage)
- `jcmd`, `jstat`, `VisualVM` for GC monitoring
- 💡 **Tip**: Don't over-tune — start with defaults and measure first

---

### Chapter 12: JSON Logging with Logback and Logstash

**Goal**: Set up structured logging — a must for any production application.

**Prerequisite note**: Basic knowledge of logging (`System.out.println` is not enough in production).

**Content**:
- Why structured (JSON) logging? Searchability in log aggregators (ELK, Splunk, Datadog)
- Logback as the de-facto standard logging backend in the Java ecosystem
- SLF4J as the logging facade — `LoggerFactory.getLogger(MyClass.class)`
- Dependencies in `build.gradle.kts`:
  ```kotlin
  implementation("ch.qos.logback:logback-classic:1.4.x")
  implementation("net.logstash.logback:logstash-logback-encoder:7.x")
  ```
- `logback.xml` configuration:
  ```xml
  <configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
      <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    <root level="INFO">
      <appender-ref ref="JSON"/>
    </root>
  </configuration>
  ```
- Adding structured fields: MDC (Mapped Diagnostic Context) — `MDC.put("userId", userId)`
- Logging levels: TRACE, DEBUG, INFO, WARN, ERROR — when to use each
- ⚠️ **Watch Out**: Don't log sensitive data (passwords, tokens, PII)
- 💡 **Tip**: Use parameterized logging — `log.info("User {} logged in", userId)` — not string concatenation

---

## Cross-Cutting Concerns (include throughout chapters)

- **Code style**: Use IntelliJ's built-in formatter and `google-java-format` or `palantir-java-format`
- **Immutability**: Prefer immutable objects — Records, `List.of()`, `final` fields
- **Null safety**: Use `Optional` rather than returning `null`; use `Objects.requireNonNull()`
- **Modern idioms**: Lambdas and method references over anonymous classes
- **Avoid common anti-patterns**: raw types, empty catch blocks, mutable statics, `System.out.println` for logging

---

## Navigation Structure

Each chapter page has:
- A sidebar or top nav listing all chapters
- `← Previous Chapter` | `Next Chapter →` links at the bottom
- A "Back to Top" anchor link for long pages

---

## Verification Checklist (before publishing each chapter)

- [ ] All code examples compile against Java 21
- [ ] Code examples are self-contained (no hidden dependencies)
- [ ] Each chapter has a "Key Takeaways" section
- [ ] Beginner-unfriendly jargon is explained inline
- [ ] Links to relevant official Javadoc pages included
- [ ] At least one 💡 **Tip** or ⚠️ **Watch Out** callout per chapter
