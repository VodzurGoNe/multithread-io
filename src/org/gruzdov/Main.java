package org.gruzdov;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Main {

    private static final String FILE_PATH = "src/out.txt";
    private static final int THREADS_COUNT = 2;

    private static final Semaphore semaphore = new Semaphore(1);
    private static long count = 0L;

    public static void main(String[] args) {
        long n = Long.parseLong(args[0]);
        if (n <= 0L || n % 2L != 0L) {
            throw new IllegalArgumentException("the number must be a multiple of 2");
        }

        var file = Path.of(FILE_PATH);
        try {
            Files.createFile(file);
            Files.write(file, "0".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        count = n;

        execute();
    }

    public static void execute() {
        var executorService = Executors.newFixedThreadPool(THREADS_COUNT);

        Runnable taskOne = Main::task;
        Runnable taskTwo = Main::task;

        executorService.execute(taskOne);
        executorService.execute(taskTwo);

        executorService.shutdown();
    }

    private static void task() {
        try {
            boolean toContinue = true;
            while (toContinue) {
                semaphore.acquire();
                toContinue = incrementValue();
                semaphore.release();

                Thread.sleep(1L);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean incrementValue() {
        try (var reader = new BufferedReader(new FileReader(FILE_PATH))) {
            long value = Long.parseLong(reader.readLine());
            if (count == value) {
                return false;
            }

            try (var writer = new FileWriter(FILE_PATH)) {
                writer.write(String.valueOf(value + 1L));
                System.out.printf("Thread: %s, old : %d, new : %d", Thread.currentThread().getName(), value, ++value);
                System.out.println();
                writer.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

}
