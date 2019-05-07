package com.github.romualdrousseau.any2json;

public interface IRow {
	boolean isEmpty(double ratio);

    int getNumberOfCells();

    int getNumberOfMergedCellsAt(int i);

    String getCellValueAt(int i);

    TableCell getCell(TableHeader header);

    TableCell getCell(TableHeader header, boolean mergeValues);

    String getCellValue(TableHeader header);

    String getCellValue(TableHeader header, boolean mergeValues);
}
