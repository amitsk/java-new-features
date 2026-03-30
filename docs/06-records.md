# Chapter 6: Records

**[← Chapter 5: Collections & Streams](05-collections-streams.md)** | **[Chapter 7: Enums & Sealed Classes →](07-enums-sealed.md)**

---

## The Problem with Plain Old Java Objects (POJOs)

For decades, Java developers have written "data carrier" classes — objects whose only job is to hold fields. Consider a simple `Point`:

```java
// The old way — a POJO
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX()  { return x; }
    public int getY()  { return y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() { return Objects.hash(x, y); }

    @Override
    public String toString() { return "Point[x=" + x + ", y=" + y + "]"; }
}
```

That's **~30 lines of boilerplate** for something that conceptually is just: "a point has an x and a y."

The popular library **Lombok** reduced this with annotations (`@Data`, `@Value`), but it's an external dependency and relies on annotation processing. Records are a first-class Java language feature — no extra tools needed.

---

## Declaring a Record

```java
// src/main/java/com/example/records/Point.java
package com.example.records;

public record Point(int x, int y) {}
```

**One line.** The compiler automatically generates:
- A **canonical constructor** `Point(int x, int y)`
- **Accessor methods** `x()` and `y()` (not `getX()` — just `x()`)
- `equals()` — two points are equal if all their components are equal
- `hashCode()` — consistent with `equals()`
- `toString()` — prints `Point[x=1, y=2]`

Usage:

```java
Point p1 = new Point(1, 2);
Point p2 = new Point(1, 2);

System.out.println(p1.x());         // 1
System.out.println(p1);             // Point[x=1, y=2]
System.out.println(p1.equals(p2));  // true
```

---

## Validating Data: Compact Constructors

Records support a **compact constructor** — a concise way to add validation without repeating the field assignments:

```java
public record Range(int min, int max) {

    // Compact constructor: no parameter list, no "this.min = min" needed
    public Range {
        if (min > max) {
            throw new IllegalArgumentException(
                "min (%d) must be <= max (%d)".formatted(min, max));
        }
    }
}
```

```java
new Range(1, 10);   // OK
new Range(10, 1);   // throws IllegalArgumentException
```

The compiler inserts `this.min = min; this.max = max;` automatically after your compact constructor body.

---

## Adding Custom Methods

Records can have instance methods — they just cannot have mutable instance fields:

```java
public record Point(int x, int y) {

    // Custom instance method
    public double distanceTo(Point other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Static factory method
    public static Point origin() {
        return new Point(0, 0);
    }
}
```

---

## Records as DTOs

Records shine as **Data Transfer Objects** — objects that carry data between layers of an application (e.g., from a REST API to the business layer):

```java
// API response body
public record UserResponse(int id, String name, String email) {}

// Usage in a controller:
UserResponse response = new UserResponse(1, "Alice", "alice@example.com");
// Jackson can serialize/deserialize records automatically
```

> 💡 **Tip — Records with Jackson (JSON)**
>
> Jackson 2.12+ supports records out of the box. No `@JsonProperty` needed — it reads from the constructor parameters. Just make sure Jackson can see the parameter names (enabled by default in modern Jackson).

---

## Records Implementing Interfaces

Records can implement interfaces — they just cannot extend another class (they implicitly extend `java.lang.Record`):

```java
public interface Shape {
    double area();
}

public record Circle(double radius) implements Shape {
    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

public record Rectangle(double width, double height) implements Shape {
    @Override
    public double area() {
        return width * height;
    }
}
```

---

## Generic Records

```java
public record Pair<A, B>(A first, B second) {
    public Pair<B, A> swap() {
        return new Pair<>(second, first);
    }
}

var point = new Pair<>("x", 42);
var swapped = point.swap();   // Pair<Integer, String>(42, "x")
```

---

## Records vs POJOs vs Lombok — Quick Comparison

| Feature | POJO | Lombok `@Value` | Record |
|---|---|---|---|
| `equals`, `hashCode`, `toString` | Write yourself | ✅ Generated | ✅ Generated |
| Immutable | Optional | ✅ | ✅ Always |
| Works in switch pattern matching | ❌ | ❌ | ✅ |
| Extends a class | ✅ | ✅ | ❌ (final) |
| JPA `@Entity` compatible | ✅ | ❌ | ❌ |
| No extra tools needed | ✅ | ❌ requires Lombok | ✅ |

---

## When NOT to Use Records

⚠️ **Don't use Records for JPA entities.** JPA requires:
- A no-argument constructor
- Mutable fields (JPA sets them via reflection after construction)

⚠️ **Don't use Records when you need inheritance.** Records are `final` — nothing can extend them.

⚠️ **Don't use Records when you need mutable state.** All record components are `final`.

For these cases, stick with regular classes (or use Lombok if you want reduced boilerplate).

---

## Key Takeaways

- A record replaces the constructor + getters + `equals` + `hashCode` + `toString` boilerplate in one line
- Compact constructors are the place to validate data in a record
- Records are immutable and final — perfect for DTOs, value objects, and data transfer
- Records work with Jackson, pattern matching, and generics
- Do **not** use records for JPA entities or classes that need inheritance

---

**[← Chapter 5: Collections & Streams](05-collections-streams.md)** | **[Chapter 7: Enums & Sealed Classes →](07-enums-sealed.md)**
