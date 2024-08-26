package com.github.romualdrousseau.any2json.base;

public interface SheetStore {

    int getLastColumnNum(int rowIndex);

    int getLastRowNum();

    boolean hasCellDataAt(int colIndex, int rowIndex);

    String getCellDataAt(int colIndex, int rowIndex);

    int getNumberOfMergedCellsAt(int colIndex, int rowIndex);
}
