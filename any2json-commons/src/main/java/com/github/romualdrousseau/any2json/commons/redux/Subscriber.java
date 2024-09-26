package com.github.romualdrousseau.any2json.commons.redux;

import java.util.function.BiConsumer;

public interface Subscriber<S, A extends Action> extends BiConsumer<Store<S, A>, A> {}
