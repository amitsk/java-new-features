# Chapter 4: Generics

**[← Chapter 3: Unit Testing](03-unit-testing.md)** | **[Chapter 5: Collections & Streams →](05-collections-streams.md)**

---

## The Problem Generics Solve

Before generics (pre-Java 5), containers like `ArrayList` stored `Object`. That meant constant casting and **runtime** type errors instead of **compile-time** ones:

```java
// Pre-generics (dangerous!)
List names = new ArrayList();
names.add("Alice");
names.add(42);            // No error here...

String name = (String) names.get(1);  // ClassCastException at runtime!
```

Generics let you tell the compiler what type a container holds, moving the error to compile time where it belongs:

```java
List<String> names = new ArrayList<>();
names.add("Alice");
names.add(42);     // Compile error: incompatible types — caught immediately
```

---

## Generic Classes

A **type parameter** (commonly `T`, `E`, `K`, `V`) is a placeholder for a concrete type:

```java
// src/main/java/com/example/generics/Box.java
package com.example.generics;

public class Box<T> {
    private T value;

    public Box(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public <R> Box<R> map(java.util.function.Function<T, R> transform) {
        return new Box<>(transform.apply(value));
    }

    @Override
    public String toString() {
        return "Box[" + value + "]";
    }
}
```

Usage:

```java
Box<String>  strBox  = new Box<>("hello");
Box<Integer> intBox  = new Box<>(42);
Box<String>  upper   = strBox.map(String::toUpperCase);

System.out.println(upper); // Box[HELLO]
```

> 💡 **Tip — The diamond operator `<>`**
>
> Since Java 7 you can write `new ArrayList<>()` instead of `new ArrayList<String>()` — the compiler infers the type. Always use the diamond; never use **raw types** like `new ArrayList()`.

---

## Generic Methods

A generic method declares its own type parameter, independent of the class:

```java
public class MathUtils {

    // T must extend Comparable so we can call compareTo()
    public static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }
}

// Works with any Comparable:
int    m1 = MathUtils.max(3, 7);          // 7
String m2 = MathUtils.max("apple", "banana"); // "banana"
```

The compiler infers `T` from the argument types — no need to write `MathUtils.<Integer>max(3, 7)`.

---

## Bounded Type Parameters

Use `extends` to restrict what types are accepted:

```java
// T must be a Number (Integer, Double, Long, etc.)
public <T extends Number> double sum(List<T> numbers) {
    return numbers.stream()
                  .mapToDouble(Number::doubleValue)
                  .sum();
}
```

Multiple bounds with `&`:

```java
public <T extends Comparable<T> & Cloneable> T safeMax(T a, T b) { ... }
```

---

## Wildcards

Wildcards let you write methods that work with families of generic types.

### Upper-bounded wildcard `<? extends T>` — "Producer"

```java
// Can READ from the list; cannot add to it
public double sumList(List<? extends Number> numbers) {
    return numbers.stream().mapToDouble(Number::doubleValue).sum();
}

sumList(List.of(1, 2, 3));        // works — Integer extends Number
sumList(List.of(1.5, 2.5));      // works — Double extends Number
```

### Lower-bounded wildcard `<? super T>` — "Consumer"

```java
// Can ADD to the list; reading gives you Object
public void addNumbers(List<? super Integer> list) {
    list.add(1);
    list.add(2);
}

List<Number> nums = new ArrayList<>();
addNumbers(nums);  // works — Number is a supertype of Integer
```

### The PECS Mnemonic

> **P**roducer **E**xtends, **C**onsumer **S**uper
>
> If a method *produces* (reads) values → use `? extends T`
> If a method *consumes* (writes) values → use `? super T`

---

## A Generic Repository Interface

A common real-world pattern — a generic data access layer:

```java
// src/main/java/com/example/generics/Repository.java
package com.example.generics;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}
```

Implementations with concrete types:

```java
public class UserRepository implements Repository<User, Integer> {
    // ...
}
```

This pattern is used extensively in Spring Data. Now you understand the `<T, ID>` in `JpaRepository<User, Long>`.

---

## Generic Records

Records (covered in Chapter 6) work with generics too:

```java
public record Pair<A, B>(A first, B second) {
    public Pair<B, A> swap() {
        return new Pair<>(second, first);
    }
}

var pair  = new Pair<>("Alice", 42);
var swapped = pair.swap();  // Pair<Integer, String>(42, "Alice")
```

---

## Type Erasure

At runtime, Java **erases** generic type information. `Box<String>` and `Box<Integer>` are both just `Box` at runtime.

Consequences:
- You **cannot** do `new T()` — the JVM doesn't know what `T` is
- You **cannot** do `instanceof List<String>` — only `instanceof List<?>`
- You **cannot** create arrays of generic types: `new T[10]` is illegal

> ⚠️ **Watch Out — Unchecked warnings**
>
> If you see `@SuppressWarnings("unchecked")` in code, it's working around type erasure. This is sometimes necessary but always means: the compiler cannot prove type safety — be careful.

---

## Key Takeaways

- Generics move `ClassCastException` from runtime to compile time
- Use `<T extends Foo>` to restrict what types a generic class/method accepts
- **PECS**: Producer Extends, Consumer Super
- Type erasure means generic info is gone at runtime — no `new T()` or `instanceof List<String>`
- Generic interfaces like `Repository<T, ID>` are a powerful abstraction pattern you'll see everywhere

---

**[← Chapter 3: Unit Testing](03-unit-testing.md)** | **[Chapter 5: Collections & Streams →](05-collections-streams.md)**
