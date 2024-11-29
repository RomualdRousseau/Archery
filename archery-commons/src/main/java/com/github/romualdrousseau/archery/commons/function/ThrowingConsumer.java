package com.github.romualdrousseau.archery.commons.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T> {

    void accept(T t) throws Exception;

    public static <V> Consumer<V> of(ThrowingConsumer<V> func) {
        return v -> {
            try {
                func.accept(v);
            } catch (Exception x) {
                throw new RuntimeException(x);
            }
        };
    }
}
