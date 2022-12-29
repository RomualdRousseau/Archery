package com.github.romualdrousseau.any2json.util;

public interface SheetStore {
    
    String getName();

    int getLastColumnNum(int rowIndex);

    int getLastRowNum();

    boolean hasCellDataAt(int colIndex, int rowIndex);

    boolean hasCellDecorationAt(int colIndex, int rowIndex);

    String getCellDataAt(int colIndex, int rowIndex);

    int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex);

    void copyCell(int colIndex1, int rowIndex1, int colIndex2, int rowIndex2);
}