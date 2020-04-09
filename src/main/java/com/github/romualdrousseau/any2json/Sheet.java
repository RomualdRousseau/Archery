package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.any2json.ITagClassifier;

public interface Sheet {

    String getName();

    Table getTable();

    Table getTable(ITagClassifier classifier);

    void addSheetListener(SheetListener listener);
}
