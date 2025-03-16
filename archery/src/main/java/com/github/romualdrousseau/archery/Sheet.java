package com.github.romualdrousseau.archery;

import java.util.Optional;

public interface Sheet {

    Document getDocument();

    String getName();

    int getLastRowNum();

    int getLastColumnNum();

    void applyTransformations();

    Optional<TableGraph> getTableGraph();

    Optional<Table> getTable();

    Optional<TableGraph> getRawTableGraph();

    Optional<Table> getRawTable();

    void addSheetListener(final SheetListener listener);
}
