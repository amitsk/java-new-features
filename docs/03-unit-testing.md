# Chapter 3: Unit Testing with JUnit 5, Mockito & AssertJ

**[← Chapter 2: Getting Started with Gradle](02-gradle.md)** | **[Chapter 4: Generics →](04-generics.md)**

---

## Why Test?

Tests let you:
- Catch bugs **before** they reach production
- Refactor code with confidence — if tests pass, behavior is preserved
- Document what your code is supposed to do
- Enable collaboration — teammates can change code without breaking things silently

We write tests **throughout** this tutorial, not as an afterthought. Getting into this habit early makes you a better programmer.

---

## Dependencies

Add to your `app/build.gradle.kts`:

```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
```

---

## Your First Test: Calculator

Start with something simple. Here's a `Calculator` class:

```java
// src/main/java/com/example/Calculator.java
package com.example;

public class Calculator {
    public int add(int a, int b)      { return a + b; }
    public int subtract(int a, int b) { return a - b; }
    public int multiply(int a, int b) { return a * b; }

    public double divide(int a, int b) {
        if (b == 0) throw new ArithmeticException("Cannot divide by zero");
        return (double) a / b;
    }
}
```

And the test class:

```java
// src/test/java/com/example/CalculatorTest.java
package com.example;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Calculator")
class CalculatorTest {

    private Calculator calc;

    @BeforeEach                      // runs before EACH test method
    void setUp() {
        calc = new Calculator();
    }

    @Test
    @DisplayName("should add two positive numbers")
    void addPositiveNumbers() {
        assertThat(calc.add(2, 3)).isEqualTo(5);
    }

    @Test
    @DisplayName("should throw when dividing by zero")
    void divideByZeroThrows() {
        assertThatThrownBy(() -> calc.divide(10, 0))
            .isInstanceOf(ArithmeticException.class)
            .hasMessage("Cannot divide by zero");
    }
}
```

Run with: `./gradlew test`

---

## JUnit 5 Annotations

| Annotation | Purpose |
|---|---|
| `@Test` | Marks a test method |
| `@DisplayName` | Human-readable name shown in IDE and reports |
| `@BeforeEach` | Runs before each test method (setup) |
| `@AfterEach` | Runs after each test method (cleanup) |
| `@BeforeAll` | Runs once before all tests in the class — must be `static` |
| `@AfterAll` | Runs once after all tests in the class — must be `static` |
| `@Disabled("reason")` | Skips the test with a recorded reason |
| `@Tag("fast")` | Labels tests for selective execution |
| `@Nested` | Groups related tests inside an outer test class |

---

## Tagging and Grouping Tests

Use `@Tag` to classify tests by cost or category:

```java
@Tag("fast")
@Test
void fastUnitTest() { ... }

@Tag("slow")
@Tag("integration")
@Test
void slowIntegrationTest() { ... }
```

Run only "fast" tests:

```bash
./gradlew test -Ptags=fast
```

Configure in `build.gradle.kts`:

```kotlin
tasks.named<Test>("test") {
    useJUnitPlatform {
        val tagsToInclude = project.findProperty("tags") as String? ?: ""
        if (tagsToInclude.isNotBlank()) includeTags(tagsToInclude)
    }
}
```

---

## Parameterized Tests

Instead of writing one test per value, use `@ParameterizedTest`:

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@DisplayName("addition")
class CalculatorAdditionTest {

    private final Calculator calc = new Calculator();

    @ParameterizedTest(name = "{index}: {0} + {1} = {2}")
    @CsvSource({
        "1,  2,  3",
        "0,  0,  0",
        "-1, 1,  0",
        "10, 20, 30"
    })
    void add(int a, int b, int expected) {
        assertThat(calc.add(a, b)).isEqualTo(expected);
    }
}
```

The `name` attribute customises the test name in reports:
```
1: 1 + 2 = 3
2: 0 + 0 = 0
3: -1 + 1 = 0
4: 10 + 20 = 30
```

### Other Parameter Sources

| Source | When to use |
|---|---|
| `@ValueSource(ints = {1,2,3})` | Single-argument tests |
| `@CsvSource({"a,1", "b,2"})` | Multiple arguments, inline |
| `@CsvFileSource(resources = "/data.csv")` | Large datasets — load from a file |
| `@MethodSource("provideData")` | Complex objects — return a `Stream<Arguments>` |

```java
static Stream<Arguments> provideData() {
    return Stream.of(
        Arguments.of("hello", 5),
        Arguments.of("world", 5)
    );
}

@ParameterizedTest
@MethodSource("provideData")
void stringLength(String s, int expected) {
    assertThat(s).hasSize(expected);
}
```

---

## AssertJ — Fluent Assertions

JUnit's built-in assertions work, but AssertJ's fluent API reads much more clearly:

```java
// JUnit built-in — reads backwards
assertEquals(5, result);
assertTrue(list.contains("alice"));

// AssertJ — reads left-to-right
assertThat(result).isEqualTo(5);
assertThat(list).contains("alice");
```

**Common assertions:**

```java
assertThat(value).isEqualTo(42);
assertThat(value).isGreaterThan(10).isLessThan(100);
assertThat(name).isNotNull().startsWith("A").endsWith("e");
assertThat(list).hasSize(3).contains("a", "b").doesNotContain("z");
assertThat(map).containsKey("name").containsEntry("age", 30);
assertThat(optional).isPresent().contains("value");

// Exception assertions
assertThatThrownBy(() -> risky()).isInstanceOf(IOException.class);
assertThatCode(() -> safe()).doesNotThrowAnyException();
```

**Soft assertions** — collect ALL failures instead of stopping at the first:

```java
import org.assertj.core.api.SoftAssertions;

SoftAssertions.assertSoftly(soft -> {
    soft.assertThat(user.name()).isEqualTo("Alice");
    soft.assertThat(user.age()).isEqualTo(30);
    soft.assertThat(user.email()).contains("@");
    // All three are verified; report shows all failures
});
```

---

## Mockito — Mocking Dependencies

A **mock** is a fake implementation of a dependency. It lets you test a class in isolation.

**Scenario:** `UserService` fetches users from a database via `UserRepository`. In tests, we don't want to hit a real database — we mock the repository.

```java
// src/main/java/com/example/UserRepository.java
package com.example;

public interface UserRepository {
    User findById(int id);
    void save(User user);
}
```

```java
// src/main/java/com/example/UserService.java
package com.example;

public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) { this.repo = repo; }

    public String getUsername(int id) {
        User user = repo.findById(id);
        if (user == null) throw new IllegalArgumentException("User not found: " + id);
        return user.name();
    }
}
```

```java
// src/test/java/com/example/UserServiceTest.java
package com.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)   // activates Mockito annotations
class UserServiceTest {

    @Mock
    UserRepository mockRepo;           // Mockito creates a fake implementation

    @InjectMocks
    UserService userService;           // Mockito injects mockRepo into userService

    @Test
    @DisplayName("should return username when user exists")
    void returnsUsernameForValidId() {
        // Arrange: stub the mock
        when(mockRepo.findById(1)).thenReturn(new User(1, "Alice"));

        // Act
        String name = userService.getUsername(1);

        // Assert
        assertThat(name).isEqualTo("Alice");
        verify(mockRepo).findById(1);   // verify the mock was called
    }

    @Test
    @DisplayName("should throw when user not found")
    void throwsForUnknownId() {
        when(mockRepo.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> userService.getUsername(99))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("99");
    }
}
```

### ArgumentCaptor

Capture what was passed into a mock method:

```java
@Test
void capturesUserOnSave() {
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

    userService.register("Bob", "bob@example.com");

    verify(mockRepo).save(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("Bob");
}
```

> ⚠️ **Watch Out — Don't mock types you don't own**
>
> Never mock `String`, `List`, `HttpClient` etc. Mock **your own interfaces**. If you need to control external behaviour, wrap it in your own interface.

> 💡 **Tip — Test structure: Arrange / Act / Assert (AAA)**
>
> Always follow the pattern: set up → execute → verify. Add blank lines between sections for readability.

---

## Key Takeaways

- `@DisplayName` makes test reports human-readable — always use it
- `@ParameterizedTest` eliminates copy-paste test methods
- AssertJ's fluent API is more readable than plain `assertEquals`
- Mockito lets you test a class in isolation by faking its dependencies
- Mock *interfaces you own*, not concrete classes or JDK types

---

**[← Chapter 2: Getting Started with Gradle](02-gradle.md)** | **[Chapter 4: Generics →](04-generics.md)**
