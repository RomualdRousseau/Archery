package com.github.romualdrousseau.archery.commons.redux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Store<S, A extends Action> {

    private final Map<A, List<Subscriber<S, A>>> subscribers = new HashMap<>();
    private final List<Reducer<S, A>> reducers = new ArrayList<>();
    private S state;

    public Store(final S state) {
        this.state = state;
    }

    public S getState() {
        return this.state;
    }

    public void addSubscriber(final A action, final Subscriber<S, A> subscriber) {
        this.subscribers.computeIfAbsent(action, x -> new ArrayList<>()).add(subscriber);
    }

    public void addReducer(final Reducer<S, A> reducer) {
        this.reducers.add(reducer);
    }

    public void dispatch(final A action) {
        @SuppressWarnings("unchecked") final var result = (A) action.get();
        this.state = reducers.stream().reduce(this.state, (x, y) -> y.apply(x, result), (x, y) -> y);
        this.subscribers.getOrDefault(result, Collections.emptyList()).forEach(x -> x.accept(this, result));
    }
}
