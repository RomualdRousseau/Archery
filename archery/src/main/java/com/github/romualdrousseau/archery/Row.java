package com.github.romualdrousseau.archery;

public interface Row {

    boolean isEmpty();

    int getRowNum();

    int getNumberOfCells();

    Cell getCellAt(final int colIndex);

    Iterable<Cell> cells();
}
