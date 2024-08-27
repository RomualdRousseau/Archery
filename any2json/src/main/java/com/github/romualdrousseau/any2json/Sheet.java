package com.github.romualdrousseau.any2json;

import java.util.Optional;

public interface Sheet {

    Document getDocument();

    String getName();

    int getLastRowNum();

    int getLastColumnNum();

    void applyTransformations();

    Optional<TableGraph> getTableGraph();

    Optional<Table> getTable();

    void addSheetListener(final SheetListener listener);
}
