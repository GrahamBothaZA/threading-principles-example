## Problem – “Parallel File Word-Count”

### Goal

Read __N__ text files in parallel, count how many times each word appears across all files, and print the top-10 most frequent words.

### Constraints

1. No `Thread` objects – use only `ExecutorService` __(Principle 1)__
2. No shared mutable `Map<String,Integer>` – use thread-safe structures __(Principle 2)__
3. Graceful shutdown – `Ctrl-C` or timeout must stop everything cleanly __(Principle 3)__
4. All tasks must react to interruption __(Principle 3)__
5. No Thread.sleep in production logic (only for demo) __(Principle 9)__
5. All exceptions must be logged, never swallowed __(Principle 10)__


### Starter Code provided (Intentionally bad) - BadWordCount.java

### Your Task – Refactor to Safe Pattern

1. Create an `ExecutorService` (fixed thread pool = number of CPU cores).
2. Submit a `Callable<Map<String,Integer>>` for each file that returns a local word-count map.
3. Collect results with `Future.get(timeout)` (e.g., 10 seconds total).
4. Merge all local maps into a `ConcurrentHashMap` (no locks).
5. Shutdown cleanly – `shutdownNow()` + `awaitTermination`.
6. Add interruption checks inside the file-reading loop.
7. Log exceptions with a proper logger (SLF4J or System.out is fine for demo).