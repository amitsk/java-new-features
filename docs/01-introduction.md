# Chapter 1: Introduction

**← [Table of Contents](../README.md)** | **[Chapter 2: Getting Started with Gradle →](02-gradle.md)**

---

## Welcome

This tutorial is your guide to **modern Java** — Java 17 through Java 25. If you've written Java before (or any object-oriented language), you're in the right place.

By the end, you'll be able to:

- Write cleaner, more expressive Java using **Records**, **Sealed classes**, and **Pattern Matching**
- Build and test projects with **Gradle** and **JUnit 5**
- Work with modern Java idioms like **Streams**, **Optional**, and **Text Blocks**
- Make HTTP calls using Java's **built-in HTTP client**
- Understand how the **JVM garbage collector** works — and which collector to pick
- Set up **structured JSON logging** for production applications

---

## Who This Tutorial Is For

| ✅ Great fit | ❌ Not the focus |
|---|---|
| CS students (2nd year+) who know Java basics | Absolute beginners with no programming experience |
| Developers coming from Python, JavaScript, or Go | Deep JVM internals (GC tuning, JIT compilation) |
| Java developers stuck on Java 8 or 11 | Exhaustive Spring Boot deep dives |

**Assumed knowledge:**
- Variables, loops, conditionals, basic OOP (classes, inheritance, interfaces)
- Basic command-line usage
- You've heard of Gradle/Maven, but deep expertise isn't required

Not there yet? Start with the [official Java Tutorials](https://docs.oracle.com/javase/tutorial/) — they're excellent. Come back when you're comfortable with classes and interfaces.

---

## Tools You'll Use

| Tool | Why | Link |
|------|-----|------|
| **JDK 25** | Latest LTS Java release | [Adoptium](https://adoptium.net/) or [SDKMAN!](https://sdkman.io/) |
| **Gradle** | Build tool — manages dependencies and compilation | [gradle.org](https://gradle.org/) |
| **IntelliJ IDEA Community** | Free, best-in-class Java IDE | [Download](https://www.jetbrains.com/idea/download/) |
| **JUnit 5** | Modern unit testing framework | [junit.org](https://junit.org/junit5/) |

> 💡 **Tip — Use SDKMAN! to manage Java versions**
>
> ```bash
> curl -s "https://get.sdkman.io" | bash
> sdk install java 25-amzn      # Amazon Corretto 25 (LTS)
> sdk use java 25-amzn
> java -version  # should print: openjdk version "25" ...
> ```
>
> SDKMAN! lets you switch between Java versions instantly — very handy when working on multiple projects.

---

## Java's Release Cadence & LTS Versions

Understanding how Java versions work is important for choosing where to invest your learning.

### The 6-Month Cadence

Since Java 9 (2017), Oracle ships **a new Java version every 6 months** — in March and September. This is a big change from the old 2–3 year release cycle.

```
Java 17 (LTS) → 18 → 19 → 20 → Java 21 (LTS) → 22 → 23 → 24 → Java 25 (LTS) → ...
  Sep '21                           Sep '23                          Sep '25
```

The rapid cadence means smaller, more focused feature sets per release — no more waiting years for a big bang.

### Long-Term Support (LTS) Releases

Not every version is suitable for production. **LTS releases** receive security patches and bug fixes for years:

| Version | Released | LTS? | Oracle Support Until |
|---------|----------|------|----------------------|
| Java 17 | Sep 2021 | ✅ | Sep 2029 |
| Java 21 | Sep 2023 | ✅ | Sep 2031 |
| **Java 25** | **Sep 2025** | **✅** | **Sep 2033** |

> 📝 **Note** — LTS releases happen every 4 versions (every 2 years). Java 17, 21, and 25 are the current trio. Java 29 will be the next LTS.

**Key rule:** Use LTS releases for production applications. Non-LTS releases (18, 19, 20, 22, 23, 24) are great for experimenting but are not maintained long-term.

### "Preview" vs. "Final" Features

Many features graduate through stages:

1. **Preview** — implemented but may change; must opt in with `--enable-preview`
2. **Second preview** — refined based on feedback
3. **Final/Standard** — stable, production-ready

Throughout this tutorial we mark features with their graduation version. Anything marked ✅ Final is safe for production use today.

---

## What's New in Java 25

Java 25 is the **current LTS release (September 2025)** and the version this tutorial targets. Here are the highlights:

| Feature | JEP | Status |
|---------|-----|--------|
| Primitive Types in Patterns | JEP 488 | ✅ Final |
| Flexible Constructor Bodies | JEP 492 | ✅ Final |
| Module Import Declarations | JEP 494 | ✅ Final |
| Structured Concurrency | JEP 505 | ✅ Final |
| Scoped Values | JEP 506 | ✅ Final |
| Stream Gatherers | JEP 485 | ✅ Final |
| Quantum-Resistant Cryptography (ML-KEM, ML-DSA) | JEP 496/497 | ✅ Final |
| Remove 32-Bit x86 Port | JEP 503 | ✅ Final |

We'll cover **Structured Concurrency**, **Scoped Values**, **Stream Gatherers**, and **Primitive Patterns** in [Chapter 8](08-java17-21-features.md).

> 💡 **Stepping Stones** — Even if you encounter Java 21 codebases at work (still very common), everything you learn here applies. Java is backward compatible. Java 21 code runs unchanged on Java 25.

---

## How to Read This Tutorial

Each chapter:
1. Starts with **why the feature matters** — not just how it works
2. Shows the **"old way"** (pre-Java 17) then the **new way** so you appreciate the improvement
3. Has **runnable code examples** — all available in `app/src/`
4. Ends with **Key Takeaways**
5. Uses callouts:
   - 💡 **Tip** — best practices and efficiency hints
   - ⚠️ **Watch Out** — common mistakes that will bite beginners
   - 📝 **Note** — deeper context, good to know but not critical

---

## Key Takeaways

- Java ships every 6 months; **LTS releases** (17, 21, 25 …) are the stable production targets
- **Java 25 (Sep 2025)** is the current LTS — this tutorial targets Java 25
- Preview features require `--enable-preview`; Final features are stable for production
- Everything in this tutorial uses code you can run starting with Chapter 2

---

**[Chapter 2: Getting Started with Gradle →](02-gradle.md)**
