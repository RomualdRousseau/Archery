package com.github.romualdrousseau.any2json.commons.redux;

import java.util.function.Supplier;

public class Action implements Supplier<Action> {

    private final String type;

    public Action(final String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public int hashCode() {
        return this.type.hashCode();
    }

    public boolean equals(final Object obj) {
        if (!(obj instanceof Action)) {
            return false;
        }
        final var otherAction = (Action) obj;
        return this.type.equals(otherAction.type);
    }

    @Override
    public Action get() {
        return this;
    }
}
