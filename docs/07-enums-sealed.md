# Chapter 7: Enums & Sealed Classes

**[← Chapter 6: Records](06-records.md)** | **[Chapter 8: Key Java 17–21 Features →](08-java17-21-features.md)**

---

## Part 1: Enums In Depth

### Basic Enums

You probably know basic enums. Here's a refresher plus the important extras:

```java
public enum Direction {
    NORTH, SOUTH, EAST, WEST;
}

Direction d = Direction.NORTH;
System.out.println(d.name());    // "NORTH"
System.out.println(d.ordinal()); // 0
```

### Enums with Fields and Methods

Enums can have fields, constructors, and methods — making them much more powerful than simple constants:

```java
// src/main/java/com/example/enums/Planet.java
public enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    VENUS  (4.869e+24, 6.0518e6),
    EARTH  (5.976e+24, 6.37814e6),
    MARS   (6.421e+23, 3.3972e6);

    private final double mass;    // in kilograms
    private final double radius;  // in meters

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    static final double G = 6.67300E-11;

    public double surfaceGravity() {
        return G * mass / (radius * radius);
    }

    public double surfaceWeight(double otherMass) {
        return otherMass * surfaceGravity();
    }
}
```

```java
double earthWeight = 75.0;
double mass = earthWeight / Planet.EARTH.surfaceGravity();
for (Planet p : Planet.values()) {
    System.out.printf("Weight on %s is %6.2f%n", p, p.surfaceWeight(mass));
}
```

### Enums Implementing Interfaces

```java
public interface Describable {
    String describe();
}

public enum Status implements Describable {
    PENDING {
        @Override public String describe() { return "Waiting to start"; }
    },
    ACTIVE {
        @Override public String describe() { return "Currently running"; }
    },
    DONE {
        @Override public String describe() { return "Finished"; }
    };
}
```

### `EnumSet` and `EnumMap`

More efficient than `HashSet<MyEnum>` and `HashMap<MyEnum, V>` — use these when keys are enum values:

```java
import java.util.EnumSet;
import java.util.EnumMap;

EnumSet<Direction> cardinals = EnumSet.of(Direction.NORTH, Direction.SOUTH);
EnumSet<Direction> all       = EnumSet.allOf(Direction.class);
EnumSet<Direction> none      = EnumSet.noneOf(Direction.class);

EnumMap<Direction, String> labels = new EnumMap<>(Direction.class);
labels.put(Direction.NORTH, "Up");
```

---

## Part 2: Pattern Matching with `switch` (Java 21)

### The Old `switch` Statement

```java
// Old: statement syntax, only works with int, String, enum
String description;
switch (status) {
    case PENDING: description = "waiting"; break;
    case ACTIVE:  description = "running"; break;
    default:      description = "unknown"; break;
}
```

### The New `switch` Expression (Java 14+)

```java
// New: expression syntax, no fall-through, arrow →
String description = switch (status) {
    case PENDING -> "waiting";
    case ACTIVE  -> "running";
    case DONE    -> "finished";
};
// No default needed if all cases are covered (enum)
```

### Pattern Matching in `switch` (Java 21 — Final)

The real power: switch on **any type**, with pattern variables:

```java
// src/main/java/com/example/sealed/ShapeArea.java
static double area(Object shape) {
    return switch (shape) {
        case Circle c       -> Math.PI * c.radius() * c.radius();
        case Rectangle r    -> r.width() * r.height();
        case Triangle t     -> 0.5 * t.base() * t.height();
        case null           -> throw new NullPointerException("shape is null");
        default             -> throw new IllegalArgumentException("Unknown: " + shape);
    };
}
```

**Guarded patterns** — add a condition with `when`:

```java
static String classify(Object obj) {
    return switch (obj) {
        case Integer i when i < 0    -> "negative int";
        case Integer i when i == 0   -> "zero";
        case Integer i               -> "positive int: " + i;
        case String s when s.isEmpty() -> "empty string";
        case String s                -> "string: " + s;
        default                      -> "other";
    };
}
```

---

## Part 3: Sealed Classes (Java 17+)

### The Problem: Uncontrolled Inheritance

When you define an interface or abstract class, **any** class in any package can implement/extend it. This is great for extensibility, but sometimes you want to model a **closed set** of types — like the sum types in functional languages.

### Declaring a Sealed Hierarchy

```java
// src/main/java/com/example/sealed/Shape.java
public sealed interface Shape
    permits Circle, Rectangle, Triangle {}

public record Circle(double radius)               implements Shape {}
public record Rectangle(double width, double height) implements Shape {}
public record Triangle(double base, double height)   implements Shape {}
```

Rules:
- `permits` lists every allowed direct subtype
- Each permitted subtype must be in the same package (or module)
- Each permitted subtype must be `final`, `sealed`, or `non-sealed`

### Exhaustive `switch` — The Killer Feature

With a sealed hierarchy, the compiler knows every possible subtype. This means a `switch` expression is **exhaustive** — no `default` needed:

```java
double area(Shape shape) {
    return switch (shape) {
        case Circle c    -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.width() * r.height();
        case Triangle t  -> 0.5 * t.base() * t.height();
        // Compiler error if Triangle is missing!
    };
}
```

If you add a new subtype to the sealed hierarchy later, the compiler will flag **every** switch that doesn't handle it. This is a huge safety improvement over uncontrolled inheritance.

### Real-World Example: Modeling HTTP Results

```java
// src/main/java/com/example/sealed/ApiResult.java
public sealed interface ApiResult<T> permits ApiResult.Success, ApiResult.Failure {

    record Success<T>(T data) implements ApiResult<T> {}
    record Failure<T>(int statusCode, String message) implements ApiResult<T> {}
}
```

```java
ApiResult<User> result = fetchUser(id);

String display = switch (result) {
    case ApiResult.Success<User> s  -> "User: " + s.data().name();
    case ApiResult.Failure<User> f  -> "Error %d: %s".formatted(f.statusCode(), f.message());
};
```

This is type-safe, the compiler checks you handle both cases, and there's no `instanceof` casting.

### `non-sealed` — Reopening Inheritance

A permitted subtype can be declared `non-sealed`, which reopens it to arbitrary subclasses:

```java
public sealed interface Vehicle permits Car, Truck, CustomVehicle {}
public record Car(String model) implements Vehicle {}
public record Truck(int payload) implements Vehicle {}
public non-sealed interface CustomVehicle extends Vehicle {}  // anyone can implement this
```

---

## Key Takeaways

- Enums can have fields, methods, and implement interfaces — they're more than constants
- Use `EnumSet` and `EnumMap` for enum-keyed collections
- `switch` expressions (Java 14+) eliminate fall-through and can return a value
- Pattern matching in `switch` (Java 21) lets you branch on type AND add guards with `when`
- Sealed classes model **closed type hierarchies** — the compiler enforces exhaustiveness in switch
- The combination of sealed interfaces + records + switch is the modern Java alternative to union types

---

**[← Chapter 6: Records](06-records.md)** | **[Chapter 8: Key Java 17–21 Features →](08-java17-21-features.md)**
