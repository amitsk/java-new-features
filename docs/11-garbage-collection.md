# Chapter 11: Garbage Collection — G1GC, ZGC & Basics

**[← Chapter 10: The New HTTP Client](10-http-client.md)** | **[Chapter 12: JSON Logging →](12-logging.md)**

---

## What Is Garbage Collection?

In C/C++, you allocate memory with `malloc` and free it with `free`. Forgetting to free causes **memory leaks**; freeing too early causes **use-after-free bugs** — both are serious.

Java's **Garbage Collector (GC)** automatically reclaims memory that is no longer reachable. You allocate objects with `new`; the JVM figures out when they're no longer referenced and frees the memory.

**Trade-off:** GC makes development safer and faster, but introduces periodic "pauses" where the application stops while the GC runs. Modern GCs minimize these pauses.

---

## Memory Layout: Stack vs. Heap

```
┌─────────────────────────────────────────────────┐
│                    JVM Memory                    │
├──────────────────┬──────────────────────────────┤
│  Thread Stacks   │       Heap Memory             │
│  (local vars,    │  ┌────────────┬────────────┐  │
│   method frames) │  │ Young Gen  │ Old Gen     │  │
│                  │  │ ┌──┬──┬──┐ │ (Tenured)  │  │
│                  │  │ │Ed│S0│S1│ │            │  │
│                  │  │ └──┴──┴──┘ │            │  │
│                  │  └────────────┴────────────┘  │
│                  │        Metaspace               │
└──────────────────┴──────────────────────────────┘
```

- **Young Generation (Eden + Survivor S0/S1)**: New objects start here. GC here is called Minor GC — fast (milliseconds), frequent
- **Old Generation (Tenured)**: Objects that survive several Minor GCs are promoted here. GC here is Major GC or Full GC — slower
- **Metaspace**: Class metadata (replaces PermGen from Java 7 and earlier)

**The Generational Hypothesis:** Most objects die young. Creating lots of short-lived objects is fine — they're collected cheaply in the Young generation.

---

## G1GC — The Default (Java 9+)

**G1** (Garbage First) replaced the Parallel GC as the default in Java 9. It's designed for **large heaps with low, predictable pause times**.

### How G1 Works

G1 divides the heap into **equal-sized regions** (1–32 MB each). Regions are dynamically designated as Eden, Survivor, or Old. This avoids the rigid generational boundaries of older collectors:

```
┌──┬──┬──┬──┬──┬──┬──┬──┐
│E │E │S │O │O │E │ │O │   E=Eden  S=Survivor  O=Old
└──┴──┴──┴──┴──┴──┴──┴──┘
```

G1 prioritises collecting the regions with the most garbage first (hence "Garbage First").

### Key JVM Flags for G1GC

```bash
-XX:+UseG1GC                  # Usually default; explicit to be sure
-XX:MaxGCPauseMillis=200      # Target max pause time (ms) — G1 tries to meet this
-Xms512m                      # Initial heap size
-Xmx4g                        # Maximum heap size
-XX:G1HeapRegionSize=16m      # Region size (auto-calculated if omitted)
```

> ⚠️ **Watch Out — `-Xmx` and `-Xms` should often match in production** to avoid heap resizing overhead and GC pressure spikes.

---

## ZGC — Sub-Millisecond Pauses (Java 21 — Production Ready)

**ZGC** is designed for applications that need **very low pause times** (sub-millisecond) even on heaps of terabytes.

### How ZGC Is Different

- Runs **concurrently** with the application — almost all GC work happens while your code runs
- Pauses are only for a few safe-points (root scanning, sync) — typically < 1ms regardless of heap size
- Supports **generational mode** in Java 21 for better throughput: `-XX:+UseZGC -XX:+ZGenerational`

### Key JVM Flags for ZGC

```bash
-XX:+UseZGC                   # Enable ZGC
-XX:+ZGenerational            # Generational ZGC (Java 21+, better throughput)
-Xms1g -Xmx8g                 # Heap sizing — same rules apply
-XX:ConcGCThreads=4           # Number of concurrent GC threads
```

---

## Shenandoah — Brief Mention

**Shenandoah** (developed by Red Hat, available in OpenJDK) is another low-pause concurrent collector, similar in goals to ZGC. Enable with `-XX:+UseShenandoahGC`. It's a solid alternative if ZGC isn't available on your JVM.

---

## Choosing a GC

| Situation | Recommended GC |
|---|---|
| General purpose, most apps | **G1GC** (default) |
| Need < 200ms pauses consistently | **G1GC** + `-XX:MaxGCPauseMillis=200` |
| Need sub-millisecond pauses | **ZGC** (Java 21) |
| Huge heaps (100 GB+) | **ZGC** |
| Batch jobs, max throughput | **Parallel GC** (`-XX:+UseParallelGC`) |

> 💡 **Tip — Measure before you tune!**
>
> Start with the default (G1GC). Enable GC logging and measure actual pause times and throughput before changing collectors. Premature GC tuning often makes things worse.

---

## GC Logging

Enable structured GC logs:

```bash
-Xlog:gc*:file=/var/log/app/gc.log:time,uptime,level,tags:filecount=5,filesize=10m
```

This writes GC events (with timestamps) to a rotating log file. Look for:
- **Pause times** — how long GC paused the application
- **Heap used before/after** — how much memory was reclaimed
- **GC frequency** — too frequent? Increase heap or reduce allocation

---

## Monitoring Tools

| Tool | What it shows |
|---|---|
| `jstat -gcutil <pid> 1000` | Live GC stats every 1 second |
| `jcmd <pid> GC.run` | Trigger a GC manually |
| `jcmd <pid> VM.flags` | Show all active JVM flags |
| **VisualVM** | GUI: heap, threads, GC, CPU profiling |
| **JDK Mission Control (JMC)** | Advanced flight recorder analysis |

---

## Key Takeaways

- The JVM's GC automatically frees unreachable objects — most objects are short-lived (Young Gen)
- **G1GC** (default since Java 9) is a great choice for most server applications
- **ZGC** achieves sub-millisecond pauses and is production-ready in Java 21
- Always **measure GC behaviour with logging** before changing collectors or flags
- Match `-Xms` and `-Xmx` in production to avoid heap resize overhead

---

**[← Chapter 10: The New HTTP Client](10-http-client.md)** | **[Chapter 12: JSON Logging →](12-logging.md)**
