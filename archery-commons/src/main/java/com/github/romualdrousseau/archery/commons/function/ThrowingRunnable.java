package com.github.romualdrousseau.archery.commons.function;

@FunctionalInterface
public interface ThrowingRunnable {

    void run() throws Exception;

    public static Runnable of(ThrowingRunnable func) {
        return () -> {
            try {
                func.run();
            } catch (Exception x) {
                throw new RuntimeException(x);
            }
        };
    }
}
