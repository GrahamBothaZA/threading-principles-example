package nl.grey;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BadWordCount {
    private static final Map<String, Integer> global = new HashMap<>();

    public static void main(String[] args) throws InterruptedException, URISyntaxException {

        List<Path> files = List.of(
                Path.of(BadWordCount.class.getResource("/data/book1.txt").toURI()),
                Path.of(BadWordCount.class.getResource("/data/book2.txt").toURI()),
                Path.of(BadWordCount.class.getResource("/data/book3.txt").toURI())
        );

        for (Path file : files) {
            new Thread(() -> countFile(file)).start();   // Raw Thread!
        }

        Thread.sleep(30_000);          // Hard-coded wait
        printTop10();
    }

    private static void countFile(Path file) {
        try {
            Files.lines(file)
                    .flatMap(line -> Arrays.stream(line.split("\\W+")))
                    .filter(w -> !w.isBlank())
                    .forEach(word -> {
                        synchronized (global) {                // Only lock inside loop!
                            global.merge(word.toLowerCase(), 1, Integer::sum);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printTop10() {
        global.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));
    }
}