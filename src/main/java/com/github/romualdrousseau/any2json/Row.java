package com.github.romualdrousseau.any2json;

public interface Row {

    boolean isEmpty();

    int getRowNum();

    int getNumberOfCells();

    Cell getCellAt(int colIndex);

    Iterable<Cell> cells();
}
