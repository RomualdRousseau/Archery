package com.github.romualdrousseau.archery.commons.behavior;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Scenario<R> {

    @FunctionalInterface
    public interface Function<U, V> {
        V apply(final Scenario<U> scenario, U u) throws Exception;
    }

    @FunctionalInterface
    public interface Supplier<V> {
        V get(final Scenario<?> scenario) throws Exception;
    }

    @FunctionalInterface
    public interface Consumer<U> {
        void accept(final Scenario<U> scenario, U u) throws Exception;
    }

    public static Scenario<Void> noParameters() throws Exception {
        return new Scenario<Void>();
    }

    public static Scenario<Void> withParameters(final Map<String, Object> parameters) throws Exception {
        return new Scenario<Void>(parameters);
    }

    private final Map<String, Object> context;

    private final R value;

    private Scenario() {
        this(Map.of(), null);
    }

    private Scenario(final Map<String, Object> context) {
        this(context, null);
    }

    private Scenario(final Map<String, Object> context, final R value) {
        this.context = new HashMap<>(context);
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(final String key) {
        return Optional.ofNullable((T) this.context.get(key));
    }

    public <T> Scenario<R> put(final String key, final T value) {
        this.context.put(key, value);
        return this;
    }

    public <T> Scenario<T> given(final Supplier<T> step) throws Exception {
        return new Scenario<T>(this.context, step.get(this));
    }

    public <T> Scenario<T> when(final Function<R, T> step) throws Exception {
        return new Scenario<T>(this.context, step.apply(this, this.value));
    }

    public Scenario<R> then(final Consumer<R> step) throws Exception {
        step.accept(this, this.value);
        return this;
    }
}
