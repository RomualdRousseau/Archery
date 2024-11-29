package com.github.romualdrousseau.archery.commons.redux;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class Test_Redux {

    @Test
    @Tag("unit")
    public void testStoreNoReducerNoSbuscriber() {
        final var state = new HashMap<String, Integer>();
        final var store = new Store<HashMap<String, Integer>, Action>(state);
        final var testAction = new Action("test");
        store.dispatch(testAction);
    }

    @Test

    public void testStoreNoReducerWithSubscriber() {
        final var state = new HashMap<String, Integer>();
        final var store = new Store<HashMap<String, Integer>, Action>(state);
        final var testAction = new Action("test");
        store.addSubscriber(testAction, (s, a) -> assertEquals("test", a.getType()));
        store.dispatch(testAction);
    }

    @Test
    @Tag("unit")
    public void testStoreWithReducerAndSubscribers() {
        final var state = new HashMap<String, Integer>();
        state.put("counter", 0);

        final var store = new Store<HashMap<String, Integer>, Action>(state);
        store.addReducer((s, a) -> {
            if (a.getType().equals("inc")) {
                s.computeIfPresent("counter", (x, y) -> y + 1);
            }
            if (a.getType().equals("dec")) {
                s.computeIfPresent("counter", (x, y) -> y - 1);
            }
            return s;
        });

        final var incAction = new Action("inc");
        final var decAction = new Action("dec");

        store.addSubscriber(incAction, (s, a) -> assertEquals(Integer.valueOf(1), s.getState().get("counter")));
        store.addSubscriber(decAction, (s, a) -> assertEquals(Integer.valueOf(0), s.getState().get("counter")));
        store.dispatch(incAction);
        store.dispatch(decAction);
    }
}
