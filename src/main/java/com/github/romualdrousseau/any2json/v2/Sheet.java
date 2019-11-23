package com.github.romualdrousseau.any2json.v2;

import com.github.romualdrousseau.any2json.ITagClassifier;

public interface Sheet {

    String getName();

    Table getTable(ITagClassifier classifier);

    void addSheetListener(SheetListener listener);
}
