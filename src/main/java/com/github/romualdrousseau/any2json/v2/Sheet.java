package com.github.romualdrousseau.any2json.v2;

import java.util.TooManyListenersException;

import com.github.romualdrousseau.any2json.ITagClassifier;

public interface Sheet {

    String getName();

    Table getTable(ITagClassifier classifier);

    void addTableListener(SheetListener listener) throws TooManyListenersException;
}
