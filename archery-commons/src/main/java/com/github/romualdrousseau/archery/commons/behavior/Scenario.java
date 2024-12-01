package com.github.romualdrousseau.archery.commons.behavior;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Scenario<R> {

    @FunctionalInterface
    public interface GivenClause {
        void given(final Scenario<?> scenario) throws Exception;
    }

    @FunctionalInterface
    public interface WhenClause<T> {
        T when(final Scenario<?> scenario) throws Exception;
    }

    @FunctionalInterface
    public interface ThenClause<T> {
        void then(final Scenario<?> scenario, final T actual) throws Exception;
    }

    public static Scenario<Void> givenNoParameters() throws Exception {
        return new Scenario<Void>(new HashMap<>(), null);
    }

    public static Scenario<Void> givenParameters(final Map<String, Object> parameters) throws Exception {
        return new Scenario<Void>(new HashMap<>(parameters), null);
    }

    public static Scenario<Void> givenScenario(final Scenario<?> parent) throws Exception {
        return new Scenario<Void>(parent.context, null);
    }

    private final Map<String, Object> context;
    private final R value;

    private Scenario(final Map<String, Object> context, final R value) {
        this.context = context;
        this.value = value;
    }

    public Map<String, Object> getContext() {
        return this.context;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(final String key) {
        return Optional.ofNullable((T) this.context.get(key));
    }

    public <T> T put(final String key, final T value) {
        this.context.put(key, value);
        return value;
    }

    public Scenario<R> given(final GivenClause step) throws Exception {
        step.given(this);
        return this;
    }

    public <T> Scenario<T> when(final WhenClause<T> step) throws Exception {
        return new Scenario<T>(this.context, step.when(this));
    }

    public Scenario<R> then(final ThenClause<R> step) throws Exception {
        step.then(this, this.value);
        return this;
    }
}
