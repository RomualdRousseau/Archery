package com.github.romualdrousseau.any2json;

public interface Row {

    boolean isEmpty();

    int getNumberOfCells();

    Iterable<Cell> cells();

    Cell getCellAt(int colIndex);
}
