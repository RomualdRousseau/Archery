package com.github.romualdrousseau.any2json.v2;

public interface IRow {

	boolean isEmpty();

    int getNumberOfCells();

    Iterable<ICell> cells();
}
