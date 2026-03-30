# Modern Java Features Tutorial

> A comprehensive, hands-on guide to modern Java (Java 17–25, current LTS) for CS students and beginner Java programmers.

---

## 📚 Table of Contents

| # | Chapter | Topics |
|---|---------|--------|
| 1 | [Introduction](docs/01-introduction.md) | Goals, tools, Java LTS cadence, Java 25 highlights |
| 2 | [Getting Started with Gradle](docs/02-gradle.md) | Project setup, wrapper, dependencies |
| 3 | [Unit Testing with JUnit 5](docs/03-unit-testing.md) | JUnit 5, Mockito, AssertJ, parameterized tests |
| 4 | [Generics](docs/04-generics.md) | Generic classes, methods, wildcards, PECS |
| 5 | [Collections, Streams & Functional Programming](docs/05-collections-streams.md) | Stream API, Collectors, parallel streams |
| 6 | [Records](docs/06-records.md) | Records vs POJOs, compact constructors, DTOs |
| 7 | [Enums & Sealed Classes](docs/07-enums-sealed.md) | Sealed hierarchies, exhaustive switch, pattern matching |
| 8 | [Key Java 17–25 Features](docs/08-java17-21-features.md) | Text blocks, var, virtual threads, stream gatherers, structured concurrency |
| 9 | [Try-with-Resources & Exceptions](docs/09-exceptions.md) | AutoCloseable, suppressed exceptions, custom exceptions |
| 10 | [The New HTTP Client](docs/10-http-client.md) | java.net.http, async requests, JSON |
| 11 | [Garbage Collection](docs/11-garbage-collection.md) | G1GC, ZGC, JVM flags, GC tuning basics |
| 12 | [JSON Logging with Logback](docs/12-logging.md) | Logback, Logstash encoder, MDC context |

---

## 🛠️ Prerequisites

- Basic Java knowledge (variables, loops, classes, OOP)
- **JDK 25** (latest LTS) — [Adoptium](https://adoptium.net/) or via [SDKMAN!](https://sdkman.io/): `sdk install java 25-amzn`
- A code editor — [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/) (free) or [VS Code + Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

Not familiar with Java basics? Start with the [official Java Tutorials](https://docs.oracle.com/javase/tutorial/) first.

---

## ▶️ Running the Examples

All code examples in this tutorial live in the `app/src/` directory.

```bash
# Clone and enter the repo
git clone <repo-url>
cd java-new-features

# Run the application
./gradlew run

# Run all tests
./gradlew test

# Run tests for a specific tag (e.g., @Tag("streams"))
./gradlew test -Ptags=streams
```

---

## 📖 How to Use This Tutorial

Each chapter:
- Starts with **why this feature matters** — not just how it works
- Shows the **"old way"** before explaining the new approach
- Has **runnable code examples** you can find in `app/src/`
- Ends with **Key Takeaways** and a link to the next chapter
- Uses these callouts throughout:
  - 💡 **Tip** — helpful hints and best practices
  - ⚠️ **Watch Out** — common mistakes and gotchas
  - 📝 **Note** — background context or deeper details

---

[Start the tutorial →](docs/01-introduction.md)
