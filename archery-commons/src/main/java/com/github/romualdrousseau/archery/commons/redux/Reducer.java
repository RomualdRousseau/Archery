package com.github.romualdrousseau.archery.commons.redux;

import java.util.function.BiFunction;

@FunctionalInterface
public interface Reducer<S, A extends Action> extends BiFunction<S, A, S> {}
