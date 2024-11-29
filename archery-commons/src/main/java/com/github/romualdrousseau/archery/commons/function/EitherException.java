package com.github.romualdrousseau.archery.commons.function;

import java.util.function.Function;

public class EitherException<T> extends Either<Exception, T> {

    public static <T> EitherException<T> of(final T r) {
        return new EitherException<T>(null, r);
    }

    public static EitherException<Void> ofRunnable(final ThrowingRunnable func) {
        try {
            func.run();
            return EitherException.<Void>of(null);
        } catch (final Exception x) {
            return EitherException.<Void>ofException(x);
        }
    }

    public static <V> EitherException<Void> ofConsumer(final ThrowingConsumer<V> func, final V v) {
        try {
            func.accept(v);
            return EitherException.<Void>of(null);
        } catch (final Exception x) {
            return EitherException.<Void>ofException(x);
        }
    }

    public static <T> EitherException<T> ofSupplier(final ThrowingSupplier<T> func) {
        try {
            return EitherException.<T>of(func.get());
        } catch (final Exception x) {
            return EitherException.<T>ofException(x);
        }
    }

    public static <V, T> EitherException<T> ofFunction(final ThrowingFunction<V, T> func, final V v) {
        try {
            return EitherException.<T>of(func.apply(v));
        } catch (final Exception x) {
            return EitherException.<T>ofException(x);
        }
    }

    public static <T> EitherException<T> ofException(final Exception e) {
        return new EitherException<T>(e, null);
    }

    protected EitherException(final Exception l, final T r) {
        super(l, r);
    }

    @Override
    public <Y> EitherException<Y> map(final Function<T, Y> func) {
        return (EitherException<Y>) super.map(func);
    }

    @Override
    public <Y> EitherException<Y> flatMap(final Function<T, Either<Exception, Y>> func) {
        return (EitherException<Y>) super.flatMap(func);
    }

    public T orElseGet(final Function<Exception, T> func) {
        if (this.getRight().isPresent()) {
            return this.getRight().get();
        }
        else if (this.getLeft().isPresent()) {
            return func.apply(this.getLeft().get());
        } else {
            return null;
        }
    }

    public T orElseThrow() throws Exception {
        if (this.getRight().isPresent()) {
            return this.getRight().get();
        }
        else if (this.getLeft().isPresent()) {
            throw this.getLeft().get();
        } else {
            return null;
        }
    }

    public T orElseThrow(final Function<Exception, Exception> func) throws Exception {
        if (this.getRight().isPresent()) {
            return this.getRight().get();
        }
        else if (this.getLeft().isPresent()) {
            throw func.apply(this.getLeft().get());
        } else {
            return null;
        }
    }

    public T orElseThrowUnchecked() {
        if (this.getRight().isPresent()) {
            return this.getRight().get();
        }
        else if (this.getLeft().isPresent()) {
            throw new RuntimeException(this.getLeft().get());
        } else {
            return null;
        }
    }

    public T orElseThrowUnchecked(final Function<Exception, Exception> func) {
        if (this.getRight().isPresent()) {
            return this.getRight().get();
        }
        else if (this.getLeft().isPresent()) {
            throw new RuntimeException(func.apply(this.getLeft().get()));
        } else {
            return null;
        }
    }
}
