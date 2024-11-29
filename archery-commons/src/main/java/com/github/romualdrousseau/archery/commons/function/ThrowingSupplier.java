package com.github.romualdrousseau.archery.commons.function;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<R> {

    R get() throws Exception;

    public static <T> Supplier<T> of(ThrowingSupplier<T> func) {
        return () -> {
            try {
                return func.get();
            } catch (Exception x) {
                throw new RuntimeException(x);
            }
        };
    }
}
