## How to create and use an ExecutorService

### 1. Create an ExecutorService

Most Common: Fixed Thread Pool

```java
import java.util.concurrent.*;

ExecutorService executor = Executors.newFixedThreadPool(4);
```

|Method|Use Case|
|----|----|
|newFixedThreadPool(n)|Fixed number of threads|
|newCachedThreadPool()|Grows/shrinks automatically|
|newSingleThreadExecutor()|"One thread, sequential"|
|newScheduledThreadPool(n)|For delayed/scheduled tasks|

### 2. Submit Tasks

Option A: `submit(Runnable)` – Fire and forget

```java
executor.submit(() -> {
System.out.println("Task running on: " + Thread.currentThread().getName());
});
```

Option B: `submit(Callable<T>)` – Get result

```java
Future<String> future = executor.submit(() -> {
    Thread.sleep(1000);
    return "Done!";
});

String result = future.get(); // Blocks until done
System.out.println(result);
```

### 3. Shutdown Gracefully (CRITICAL!)

```java
executor.shutdown(); // Allows running tasks to finish

// OR: Wait for tasks to finish
executor.awaitTermination(10, TimeUnit.SECONDS);
```

> Never skip shutdown — prevents app from exiting and memory leaks.

### Full Working Example

```java
import java.util.concurrent.*;

public class ExecutorExample {
    public static void main(String[] args) throws Exception {
        // 1. Create thread pool with 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 2. Submit 5 tasks
        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " started by " 
                    + Thread.currentThread().getName());
                try { Thread.sleep(1000); } catch (Exception e) {}
                System.out.println("Task " + taskId + " finished");
            });
        }

        // 3. Shutdown
        executor.shutdown();
        System.out.println("Shutdown initiated...");

        // Wait up to 10 seconds
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow(); // Force stop
        }

        System.out.println("All tasks completed!");
    }
}
```