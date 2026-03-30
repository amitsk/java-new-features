# Chapter 2: Getting Started with Gradle

**[← Chapter 1: Introduction](01-introduction.md)** | **[Chapter 3: Unit Testing →](03-unit-testing.md)**

---

## Why a Build Tool?

Imagine you have 20 Java files spread across several packages, plus a handful of third-party libraries (JARs). You could compile everything by hand:

```bash
javac -cp lib/jackson.jar:lib/guava.jar -d out src/com/example/*.java
```

That gets painful fast. A **build tool** handles:
- Compiling your code in the right order
- Downloading and managing libraries (dependencies)
- Running tests
- Packaging your app into a runnable JAR

**Gradle** is the modern choice for Java. It's faster than Maven (incremental builds, build cache) and uses a concise Kotlin or Groovy DSL.

---

## Installing the JDK

### Recommended: SDKMAN! (macOS / Linux)

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.3-tem   # Eclipse Temurin (OpenJDK)
sdk use java 21.0.3-tem
java -version
```

### Windows

Download the installer from [Adoptium](https://adoptium.net/), run it, and ensure the JDK `bin/` folder is on your `PATH`.

### Verify

```bash
java -version
# openjdk version "21.0.3" 2024-04-16
javac -version
# javac 21.0.3
```

> ⚠️ **Watch Out — `JAVA_HOME`**
>
> Many tools (Gradle, Maven, IDEs) rely on the `JAVA_HOME` environment variable. Set it to your JDK directory:
> ```bash
> export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
> ```
> Add this to your `~/.bashrc` or `~/.zshrc` so it persists.

---

## Creating a New Project

```bash
mkdir my-java-app && cd my-java-app
gradle init --type java-application --dsl kotlin
```

Answer the prompts:
- **Target Java version:** `21`
- **Project structure:** `Single application project`
- **Test framework:** `JUnit Jupiter`

---

## Understanding the Generated Files

```
my-java-app/
├── gradlew                  # Gradle wrapper script (Linux/macOS)
├── gradlew.bat              # Gradle wrapper script (Windows)
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties   # Which Gradle version to use
├── settings.gradle.kts      # Project name
└── app/
    ├── build.gradle.kts     # Build configuration for the 'app' subproject
    └── src/
        ├── main/java/com/example/
        │   └── App.java     # Your application code
        └── test/java/com/example/
            └── AppTest.java # Your tests
```

### `settings.gradle.kts`

```kotlin
rootProject.name = "my-java-app"
include("app")
```

Defines the project name and lists subprojects. Multi-module projects have multiple entries here.

### `app/build.gradle.kts`

```kotlin
plugins {
    application    // Adds 'run' task and creates an executable JAR
}

repositories {
    mavenCentral() // Download dependencies from Maven Central
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.example.App"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
```

### The Gradle Wrapper (`gradlew`)

> 💡 **Tip — Always use `./gradlew`, not `gradle`**
>
> The wrapper downloads the exact Gradle version specified in `gradle-wrapper.properties`. This means everyone on the team (and CI) uses the same Gradle version regardless of what's installed globally. Always commit the wrapper files.

---

## Setting the Java Toolchain

The `java { toolchain { ... } }` block tells Gradle which JDK to use. Gradle can even **download the JDK automatically** if it isn't installed:

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ADOPTIUM  // optional: pin the vendor
    }
}
```

This is far better than relying on `JAVA_HOME` because it's captured in source control.

---

## Adding Dependencies

Dependencies are listed in the `dependencies { }` block of `build.gradle.kts`. Add Jackson for JSON handling:

```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("org.assertj:assertj-core:3.25.3")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}
```

**Configuration types:**

| Configuration | Used when |
|---|---|
| `implementation` | Your main source code needs the library |
| `testImplementation` | Only your test code needs it |
| `testRuntimeOnly` | Only needed at test runtime (not compile time) |
| `runtimeOnly` | Only needed at runtime, not compile time |

After changing dependencies, run `./gradlew dependencies` to see the full dependency tree.

---

## Essential Gradle Tasks

```bash
./gradlew run           # Compile and run the application
./gradlew test          # Compile and run tests
./gradlew build         # Compile, test, and build a JAR
./gradlew clean         # Delete all build output
./gradlew dependencies  # Print dependency tree
./gradlew tasks         # List all available tasks
```

> 💡 **Tip — Use `--info` or `--debug` when something goes wrong**
>
> ```bash
> ./gradlew test --info
> ```
> This prints detailed logs including why a test failed to compile.

---

## Incremental Builds

Gradle tracks input/output of each task. If source files haven't changed since the last build, it skips the task:

```
> Task :app:compileJava UP-TO-DATE
> Task :app:test UP-TO-DATE
```

`UP-TO-DATE` means "nothing changed — skipping". This makes builds fast, especially in large projects.

---

## Key Takeaways

- Gradle replaces manual `javac` + classpath juggling with a clean build script
- Always use `./gradlew` (the wrapper), never a globally installed Gradle
- The `java { toolchain { } }` block pins your Java version in source control
- `implementation` is for app code; `testImplementation` is for test libraries only

---

**[← Chapter 1: Introduction](01-introduction.md)** | **[Chapter 3: Unit Testing →](03-unit-testing.md)**
