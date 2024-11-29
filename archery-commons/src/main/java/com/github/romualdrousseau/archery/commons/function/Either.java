package com.github.romualdrousseau.archery.commons.function;

import java.util.Optional;
import java.util.function.Function;

public class Either<L, R> {
    public static <L, R> Either<L, R> ofLeft(final L l) {
        return new Either<L, R>(l, null);
    }
    public static <L, R> Either<L, R> ofRight(final R r) {
        return new Either<L, R>(null, r);
    }

    private final Optional<L> left;

    private final Optional<R> right;

    protected Either(final L l, final R r) {
        this.left = Optional.ofNullable(l);
        this.right = Optional.ofNullable(r);
    }

    public Optional<L> getLeft() {
        return this.left;
    }

    public Optional<R> getRight() {
        return this.right;
    }

    public <Y> Either<L, Y> map(final Function<R, Y> func) {
        if (this.right.isPresent()) {
            return Either.<L, Y>ofRight(func.apply(this.right.get()));
        }
        else if (this.left.isPresent()) {
            return Either.<L, Y>ofLeft(this.left.get());
        } else {
            throw new RuntimeException("Either is in illegal state as both sides are not present");
        }
    }

    public <Y> Either<L, Y> flatMap(final Function<R, Either<L, Y>> func) {
        if (this.right.isPresent()) {
            return func.apply(this.right.get());
        }
        else if (this.left.isPresent()) {
            return Either.<L, Y>ofLeft(this.left.get());
        } else {
            throw new RuntimeException("Either is in illegal state as both sides are not present");
        }
    }

    public R orElseGet(final Function<L, R> func) {
        if (this.right.isPresent()) {
            return this.right.get();
        }
        else if (this.left.isPresent()) {
            return func.apply(this.left.get());
        } else {
            throw new RuntimeException("Either is in illegal state as both sides are not present");
        }
    }
}
