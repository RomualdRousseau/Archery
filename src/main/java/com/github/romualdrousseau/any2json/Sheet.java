package com.github.romualdrousseau.any2json;

public interface Sheet {

    String getName();

    Table getTable();

    void addSheetListener(SheetListener listener);
}
