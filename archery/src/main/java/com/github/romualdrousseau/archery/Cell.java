package com.github.romualdrousseau.archery;

public interface Cell {

    boolean hasValue();

    String getValue();

    Iterable<String> entities();

    String getEntitiesAsString();
}
