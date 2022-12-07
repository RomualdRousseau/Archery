package com.github.romualdrousseau.any2json;

public interface Row {

    boolean isEmpty();

    int getRowNum();

    Iterable<Cell> cells();

    int getNumberOfCells();

    Cell getCellAt(int colIndex);
}
