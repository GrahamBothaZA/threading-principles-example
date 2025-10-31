package nl.grey;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class GivenSolution {
    private static final int TIMEOUT_SECONDS = 10;
    private static final ConcurrentHashMap<String, Integer> totals = new ConcurrentHashMap<>();

    public static void main(String[] args) throws URISyntaxException {
        List<Path> files = List.of(
                Path.of(MyWordCount.class.getResource("/data/book1.txt").toURI()),
                Path.of(MyWordCount.class.getResource("/data/book2.txt").toURI()),
                Path.of(MyWordCount.class.getResource("/data/book3.txt").toURI())
        );

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(threads);

        List<Future<Map<String, Integer>>> futures = files.stream()
                .map(file -> exec.submit(() -> countFileSafe(file)))
                .toList();

        // Wait for all (with timeout)
        for (Future<Map<String, Integer>> f : futures) {
            try {
                Map<String, Integer> local = f.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                local.forEach((word, cnt) ->
                        totals.merge(word, cnt, Integer::sum));
            } catch (TimeoutException e) {
                System.err.println("Task timed out – cancelling remaining work");
                f.cancel(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException e) {
                System.err.println("Task failed: " + e.getCause());
            }
        }

        exec.shutdownNow();
        try {
            if (!exec.awaitTermination(2, TimeUnit.SECONDS)) {
                System.err.println("Executor did not terminate");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        printTop10();
    }

    private static Map<String, Integer> countFileSafe(Path file) throws IOException {
        Map<String, Integer> local = new HashMap<>();

        Files.lines(file)
                .takeWhile(line -> !Thread.currentThread().isInterrupted()) // respect cancel
                .flatMap(line -> Arrays.stream(line.split("\\W+")))
                .filter(w -> !w.isBlank())
                .map(String::toLowerCase)
                .forEach(w -> local.merge(w, 1, Integer::sum));

        return local;
    }

    private static void printTop10() {
        totals.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));
    }
}