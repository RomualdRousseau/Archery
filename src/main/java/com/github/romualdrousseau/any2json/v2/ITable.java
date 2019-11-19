package com.github.romualdrousseau.any2json.v2;

public interface ITable {

    int getNumberOfColumns();

    int getNumberOfRows();

    Iterable<IRow> rows();

    Iterable<ICell> cells();
}
