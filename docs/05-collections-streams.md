# Chapter 5: Collections, Streams & Functional Programming

**[← Chapter 4: Generics](04-generics.md)** | **[Chapter 6: Records →](06-records.md)**

---

## Overview

This is one of the most practically important chapters. The Collection and Stream APIs are used in virtually every Java application. You'll learn:

- How to create and choose the right collection type
- How to process data declaratively with Streams
- Functional programming concepts Java uses: lambdas, method references, `Optional`
- Parallel processing with `.parallel()`

---

## Part 1: Collections

### Collection Types at a Glance

| Interface | Characteristics | Common implementations |
|---|---|---|
| `List` | Ordered, allows duplicates | `ArrayList`, `LinkedList` |
| `Set` | No duplicates | `HashSet`, `LinkedHashSet`, `TreeSet` |
| `Map` | Key-value pairs, keys unique | `HashMap`, `LinkedHashMap`, `TreeMap` |
| `Queue` | FIFO processing | `ArrayDeque`, `PriorityQueue` |

### Factory Methods (Java 9+) — Immutable Collections

```java
// Immutable — you CANNOT add/remove elements after creation
List<String> fruits  = List.of("apple", "banana", "cherry");
Set<Integer> primes  = Set.of(2, 3, 5, 7, 11);
Map<String, Integer> ages = Map.of("Alice", 30, "Bob", 25);

// For maps with more than 10 entries, use Map.ofEntries()
Map<String, Integer> big = Map.ofEntries(
    Map.entry("a", 1),
    Map.entry("b", 2),
    Map.entry("c", 3)
    // ... up to as many as you need
);
```

> ⚠️ **Watch Out — `List.of()` is immutable!**
>
> ```java
> List<String> list = List.of("a", "b");
> list.add("c");           // throws UnsupportedOperationException!
> list.set(0, "x");        // also throws!
> ```
>
> For a mutable list, use `new ArrayList<>(List.of("a", "b"))`.

### Copying Collections (Java 10+)

```java
List<String> original = new ArrayList<>(List.of("a", "b", "c"));
original.add("d");
List<String> copy = List.copyOf(original);  // immutable snapshot
```

### Choosing the Right Implementation

| Situation | Pick |
|---|---|
| Default indexed list | `ArrayList` |
| Frequent insertion/deletion at start/middle | `LinkedList` |
| Fast lookup, don't care about order | `HashMap` |
| Need insertion-ordered map | `LinkedHashMap` |
| Need sorted keys | `TreeMap` |
| Fast unique-element set | `HashSet` |

### Useful Map Methods

```java
Map<String, Integer> scores = new HashMap<>();
scores.put("Alice", 85);

scores.getOrDefault("Bob", 0);           // 0 — Bob doesn't exist yet
scores.putIfAbsent("Alice", 100);        // does nothing — Alice exists
scores.computeIfAbsent("Bob",            // compute and store if missing
    k -> k.length() * 10);              // Bob → 30
scores.merge("Alice", 10,
    Integer::sum);                       // Alice: 85 + 10 = 95
```

### Iterating Safely with Iterator

A regular `for-each` loop throws `ConcurrentModificationException` if you remove elements. Use `Iterator`:

```java
List<String> names = new ArrayList<>(List.of("Alice", "Bob", "Carol"));
Iterator<String> it = names.iterator();
while (it.hasNext()) {
    if (it.next().startsWith("B")) {
        it.remove();   // safe removal during iteration
    }
}
```

---

## Part 2: Streams

### What Is a Stream?

A Stream is a **pipeline** for processing a sequence of elements. Key properties:
- **Lazy** — intermediate operations don't execute until a terminal operation is called
- **Single-use** — once consumed, a stream cannot be reused
- **Non-destructive** — the source collection is not modified

```
Source → [filter] → [map] → [sorted] → Terminal
                   intermediate            eager
                   (lazy)
```

### Creating Streams

```java
List<String> names = List.of("Alice", "Bob", "Carol");
names.stream()                          // from a collection
Stream.of("a", "b", "c")               // from values
Arrays.stream(new int[]{1, 2, 3})      // from array
IntStream.range(0, 10)                 // 0..9
IntStream.rangeClosed(1, 10)           // 1..10
Files.lines(Path.of("data.txt"))       // lines from a file
Stream.iterate(0, n -> n + 2).limit(5) // 0, 2, 4, 6, 8
```

### Intermediate Operations (Lazy)

These return a Stream and don't do any work until a terminal is called:

```java
List<String> names = List.of("Alice", "Bob", "Charlie", "anna");

names.stream()
     .filter(n -> n.length() > 3)    // keep names longer than 3 chars
     .map(String::toUpperCase)        // transform each element
     .sorted()                        // sort alphabetically
     .distinct()                      // remove duplicates
     .limit(5)                        // take at most 5
     .skip(1)                         // skip the first result
     .forEach(System.out::println);   // terminal — triggers evaluation
```

### `flatMap` — Flattening Nested Structures

`map` transforms each element to one output. `flatMap` transforms each element to a Stream, then flattens all those streams into one:

```java
List<List<String>> nested = List.of(
    List.of("a", "b"),
    List.of("c", "d"),
    List.of("e")
);

List<String> flat = nested.stream()
    .flatMap(Collection::stream)
    .collect(Collectors.toList());
// ["a", "b", "c", "d", "e"]
```

Real-world: collect all words from a list of sentences:

```java
List<String> sentences = List.of("hello world", "foo bar baz");
List<String> words = sentences.stream()
    .flatMap(s -> Arrays.stream(s.split(" ")))
    .collect(Collectors.toList());
// ["hello", "world", "foo", "bar", "baz"]
```

### Terminal Operations (Eager — triggers evaluation)

```java
stream.toList()                        // Java 16+: collect to unmodifiable List
stream.collect(Collectors.toList())    // mutable List
stream.collect(Collectors.toSet())
stream.collect(Collectors.joining(", "))   // "Alice, Bob, Carol"
stream.count()                         // long
stream.findFirst()                     // Optional<T>
stream.findAny()                       // Optional<T> — better for parallel
stream.anyMatch(s -> s.startsWith("A")) // boolean
stream.allMatch(s -> s.length() > 2)    // boolean
stream.noneMatch(s -> s.isEmpty())      // boolean

// Numeric terminals (use IntStream/LongStream/DoubleStream):
IntStream.of(1, 2, 3, 4, 5).sum()          // 15
IntStream.of(1, 2, 3, 4, 5).average()      // OptionalDouble(3.0)
IntStream.of(1, 2, 3, 4, 5).max()          // OptionalInt(5)
IntStream.of(1, 2, 3, 4, 5).summaryStatistics()  // count, sum, min, max, avg
```

### Collectors — Powerful Data Grouping

```java
List<String> names = List.of("Alice", "Bob", "Charlie", "Anna", "Brian");

// Group by first letter
Map<Character, List<String>> byLetter = names.stream()
    .collect(Collectors.groupingBy(s -> s.charAt(0)));
// {A=[Alice, Anna], B=[Bob, Brian], C=[Charlie]}

// Count per group
Map<Character, Long> countByLetter = names.stream()
    .collect(Collectors.groupingBy(s -> s.charAt(0), Collectors.counting()));
// {A=2, B=2, C=1}

// Partition into two groups
Map<Boolean, List<String>> shortLong = names.stream()
    .collect(Collectors.partitioningBy(s -> s.length() <= 3));
// {true=[Bob], false=[Alice, Charlie, Anna, Brian]}

// Join to string
String joined = names.stream()
    .collect(Collectors.joining(", ", "[", "]"));
// "[Alice, Bob, Charlie, Anna, Brian]"
```

### Optional — Avoiding NullPointerException

`Optional<T>` is returned by methods like `findFirst()` and `min()`. It represents a value that may or may not be present:

```java
Optional<String> first = names.stream()
    .filter(s -> s.startsWith("Z"))
    .findFirst();

first.isPresent();           // false
first.isEmpty();             // true (Java 11+)
first.orElse("unknown");     // "unknown"
first.orElseThrow();         // throws NoSuchElementException if empty
first.ifPresent(System.out::println); // does nothing if empty

// Chain operations safely
Optional<Integer> nameLength = first.map(String::length);
```

> ⚠️ **Watch Out — Don't call `get()` without checking**
>
> ```java
> optional.get()  // throws NoSuchElementException if empty!
> ```
> Always use `orElse()`, `orElseThrow()`, or `ifPresent()` instead.

---

## Part 3: Parallel Streams

`.parallel()` splits the stream across multiple CPU cores using a `ForkJoinPool`:

```java
long count = names.parallelStream()
    .filter(n -> n.startsWith("A"))
    .count();
```

### When to Use `.parallel()`

✅ Large datasets (millions of elements)  
✅ CPU-bound operations (mathematical computations)  
✅ No shared mutable state  

❌ Small datasets — overhead of thread coordination exceeds the benefit  
❌ Sequential-dependent operations  
❌ I/O-bound operations (use virtual threads instead — see Chapter 8)

> ⚠️ **Watch Out — Ordering**
>
> Parallel streams may process elements in any order. `findFirst()` becomes `findAny()`. Use `forEachOrdered()` instead of `forEach()` if order matters.

---

## Part 4: Performance Tips

1. **Use primitive streams** to avoid boxing/unboxing overhead:
   ```java
   // Slow: autoboxes each int to Integer
   list.stream().mapToObj(i -> i).reduce(0, Integer::sum)
   
   // Fast: stays as primitive int
   list.stream().mapToInt(Integer::intValue).sum()
   ```

2. **Short-circuit early**: `findFirst()`, `anyMatch()`, `limit()` stop processing as soon as they can

3. **Don't use `peek()` for side effects** in production — it's a debugging tool and its behaviour in parallel streams is unreliable

4. **Avoid stateful intermediate operations** in parallel streams (`sorted()`, `distinct()`) — they require buffering all elements

---

## Key Takeaways

- `List.of()`, `Set.of()`, `Map.of()` create **immutable** collections — use `new ArrayList<>(List.of(...))` for mutable
- Streams are **lazy** — nothing runs until a terminal operation is called
- `flatMap` flattens nested streams; `groupingBy` and `partitioningBy` are powerful grouping collectors
- Use `Optional` to avoid null — never call `.get()` without checking
- Parallel streams help for CPU-bound, large-dataset work; measure before using

---

**[← Chapter 4: Generics](04-generics.md)** | **[Chapter 6: Records →](06-records.md)**
