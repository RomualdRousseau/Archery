package com.github.romualdrousseau.any2json.v2;

public interface Row {

	boolean isEmpty();

    int getNumberOfCells();

    Iterable<Cell> cells();
}
