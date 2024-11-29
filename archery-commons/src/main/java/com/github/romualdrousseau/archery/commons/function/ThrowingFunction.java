package com.github.romualdrousseau.archery.commons.function;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R> {

    R apply(T t) throws Exception;

    public static <V, T> Function<V, T> of(ThrowingFunction<V, T> func) {
        return v -> {
            try {
                return func.apply(v);
            } catch (Exception x) {
                throw new RuntimeException(x);
            }
        };
    }
}
