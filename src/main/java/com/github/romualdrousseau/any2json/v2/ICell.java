package com.github.romualdrousseau.any2json.v2;

import com.github.romualdrousseau.shuju.math.Vector;

public interface ICell {

    boolean hasValue();

    String getValue();

    ICell setValue(String value);

    String getCleanValue();

    Vector getEntityVector();

    int getMergedCount();
}
