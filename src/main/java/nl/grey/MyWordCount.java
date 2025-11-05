package nl.grey;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class MyWordCount {
    private static final Map<String, Integer> globalWordCounts = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException, URISyntaxException, ExecutionException {

        List<Path> files = List.of(
                Path.of(MyWordCount.class.getResource("/data/book1.txt").toURI()),
                Path.of(MyWordCount.class.getResource("/data/book2.txt").toURI()),
                Path.of(MyWordCount.class.getResource("/data/book3.txt").toURI())
        );

        // 1. Create thread pool with 3 threads - you can match this thread pool with the amount of CPU cores for optimal results
        ExecutorService executor = Executors.newFixedThreadPool(files.size());
        List<Future<Map<String,Integer>>> futureList = new ArrayList<>();

        for (Path file : files) {
            // 2 a. Create each book's task

            Callable<Map<String,Integer>> task = () -> {
                System.out.println("Task " + file.getFileName() + " started by " + Thread.currentThread().getName());

                try { Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5001)); } catch (Exception e) {}

                return countedWordsFromFile(file);
            };

            // 2 b. Submit each book's tasks to the executor

            Future<Map<String,Integer>> future = executor.submit(task);
            futureList.add(future);
        }

        for (Future<Map<String,Integer>> future : futureList) {
            // 2 c. Get the result from each task and combine the results to the global results

            try {
                Map<String,Integer> result = future.get(3, TimeUnit.SECONDS);
                result.forEach((k, v) -> globalWordCounts.merge(k, v, Integer::sum));
            } catch (TimeoutException e) {
                System.out.println("A task took too long!");
                future.cancel(true); // Interrupt if running
                break;
            }
        }

        // 3. Shutdown
        executor.shutdown();
        System.out.println("Shutdown initiated...");

        // Wait up to 10 seconds
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow(); // Force stop
        }

        System.out.println("All tasks completed!");

        printTop10();
    }

    private static Map<String,Integer> countedWordsFromFile(Path file) {
        Map<String,Integer> countedWords = new HashMap<>();
        try {
            Files.lines(file)
                    .flatMap(line -> Arrays.stream(line.split("\\W+")))
                    .filter(w -> !w.isBlank())
                    .forEach(word -> {
                        countedWords.merge(word.toLowerCase(), 1, Integer::sum);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return countedWords;
    }

    private static void printTop10() {
        globalWordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));
    }
}