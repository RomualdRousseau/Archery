package com.github.romualdrousseau.any2json;

public interface IRow {
    ITable getTable();

    int getGroupId();

	boolean isEmpty(double ratio);

    int getNumberOfCells();

    int getNumberOfMergedCellsAt(int imdex);

    TableCell getCellAt(int index);

    TableCell getCell(IHeader header);

    TableCell getCell(IHeader header, boolean mergeValues);

    String getCellValueAt(int index);

    String getCellValue(IHeader header);

    String getCellValue(IHeader header, boolean mergeValues);
}
