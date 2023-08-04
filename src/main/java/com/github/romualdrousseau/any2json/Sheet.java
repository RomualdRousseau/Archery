package com.github.romualdrousseau.any2json;

public interface Sheet
{
    String getName();

    int getLastRowNum();

    int getLastColumnNum();

    Table getTable();

    void setClassifierFactory(final ClassifierFactory classifierFactory);

    void addSheetListener(final SheetListener listener);
}
