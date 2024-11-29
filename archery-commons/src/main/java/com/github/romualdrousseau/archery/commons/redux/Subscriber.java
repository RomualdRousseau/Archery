package com.github.romualdrousseau.archery.commons.redux;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface Subscriber<S, A extends Action> extends BiConsumer<Store<S, A>, A> {}
