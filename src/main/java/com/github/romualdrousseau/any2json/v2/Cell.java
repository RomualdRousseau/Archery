package com.github.romualdrousseau.any2json.v2;

import com.github.romualdrousseau.shuju.math.Vector;

public interface Cell {

    boolean hasValue();

    String getValue();

    Vector getEntityVector();

    int getMergedCount();
}
