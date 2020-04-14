package com.github.romualdrousseau.any2json;

public interface Sheet {

    String getName();

    Table getTable();

    Table getTable(ITagClassifier classifier);

    void addSheetListener(SheetListener listener);
}
