package com.github.romualdrousseau.any2json.v2;

public interface Row {

	boolean isEmpty();

    int getNumberOfCells();

    Iterable<Cell> cells();

    Cell getCellAt(int coldIndex);

    Cell getCell(Header header);

    Cell getCell(Header header, boolean merged);
}
