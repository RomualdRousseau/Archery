package com.github.romualdrousseau.any2json.v2;

public interface Header {

    String getName();

    String getValue();

    boolean hasTag();

    HeaderTag getTag();
}
