package com.github.romualdrousseau.archery.commons.time;

import com.github.romualdrousseau.archery.commons.function.ThrowingRunnable;
import com.github.romualdrousseau.archery.commons.logging.LoggerWithLabels;

public class Chrono {

    public static void watch(final LoggerWithLabels logger, final Runnable worker) {
        final var startTime = System.currentTimeMillis();
        try {

            worker.run();

            final var stopTime = System.currentTimeMillis();
            final var executionTimeInMS = (int) (stopTime - startTime);
            logger.info("Operation completed in {} ms", executionTimeInMS);

        } catch (final Throwable x) {
            final var stopTime = System.currentTimeMillis();
            final var executionTimeInMS = (int) (stopTime - startTime);
            logger.info("Operation failed in {} ms", executionTimeInMS, x);
            throw x;
        }
    }

    public static void retry(final ThrowingRunnable runnable, final int maxCount) throws Exception {
        var success = false;
        var count = 0;
        while(!success) {
            try {
                runnable.run();
                success = true;
            } catch(final Throwable x) {
                count++;
                if (count >= maxCount) {
                    throw x;
                }
                Chrono.sleep(1000 + 10 ^ count);
            }
        }
    }

    public static void sleep(final long value) {
        try {
            Thread.sleep(value);
        } catch (final InterruptedException x) {
            throw new RuntimeException(x);
        }
    }
}
