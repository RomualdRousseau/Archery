package com.github.romualdrousseau.any2json;

public interface Sheet {

    String getName();

    Table getTable(final ClassifierFactory classifierFactory);

    void addSheetListener(final SheetListener listener);
}
