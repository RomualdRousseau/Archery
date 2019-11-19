package com.github.romualdrousseau.any2json.v2;

import com.github.romualdrousseau.any2json.ITagClassifier;

public interface ISheet {

    String getName();

    ITable getTable(ITagClassifier classifier);
}
