package com.github.romualdrousseau.archery.commons.function;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Error {

    public class RetryResult<R> {
        public static <R> RetryResult<R> of() {
            return new Error().new RetryResult<R>();
        }

        public static <R> RetryResult<R> of(final Throwable throwable) {
            return new Error().new RetryResult<R>(throwable);
        }

        public static <R> RetryResult<R> of(final Optional<R> result) {
            return new Error().new RetryResult<R>(result);
        }

        private final Optional<R> result;

        private final Optional<Throwable> throwable;

        private RetryResult() {
            this.result = Optional.empty();
            this.throwable = Optional.empty();
        }

        private RetryResult(final Throwable throwable) {
            this.result = Optional.empty();
            this.throwable = Optional.of(throwable);
        }

        private RetryResult(final Optional<R> result) {
            this.result = result;
            this.throwable = Optional.empty();
        }

        public boolean isEmpty() {
            return this.result.isEmpty() && this.throwable.isEmpty();
        }

        public Optional<R> getResult() {
            return this.result;
        }

        public Optional<Throwable> getError() {
            return this.throwable;
        }
    }

    public static Optional<Throwable> wrap(final ThrowingRunnable func) {
        try {
            func.run();
            return Optional.empty();
        } catch (final Throwable x) {
            return Optional.of(x);
        }
    }

    public static Optional<Throwable> wrapAndReturn(final ThrowingSupplier<Optional<Throwable>> func) {
        try {
            return func.get();
        } catch (final Throwable x) {
            return Optional.of(x);
        }
    }

    public static <T> Function<T, Optional<Throwable>> wrapChecked(final ThrowingConsumer<T> func) {
        return (t) -> {
            try {
                func.accept(t);
                return Optional.empty();
            } catch (final Throwable x) {
                return Optional.of(x);
            }
        };
    }

    public static <T> Function<T, Optional<Throwable>> wrapCheckedAndReturn(
            final ThrowingFunction<T, Optional<Throwable>> func) {
        return (t) -> {
            try {
                return func.apply(t);
            } catch (final Throwable x) {
                return Optional.of(x);
            }
        };
    }

    public static <T> RetryResult<T> retry(final Supplier<RetryResult<T>> func, final int max) {
        var result = RetryResult.<T>of();
        for (int retry = 0; retry < max && result.getResult().isEmpty(); retry++) {
            result = func.get();
        }
        return result;
    }
}
