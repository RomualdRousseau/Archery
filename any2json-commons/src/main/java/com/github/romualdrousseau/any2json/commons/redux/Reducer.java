package com.github.romualdrousseau.any2json.commons.redux;

import java.util.function.BiFunction;

public interface Reducer<S, A extends Action> extends BiFunction<S, A, S> {}
