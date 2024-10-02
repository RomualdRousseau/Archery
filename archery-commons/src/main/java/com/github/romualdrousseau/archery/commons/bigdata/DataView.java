package com.github.romualdrousseau.archery.commons.bigdata;

import java.util.Iterator;

public class DataView implements Iterable<Row> {
    private final DataFrame dataFrame;
    private final int rowStart;
    private final int columnStart;
    private final int rowCount;
    private final int columnCount;

    public DataView(final DataFrame dataFrame, final int rowStart, final int columnStart, final int rowCount, final int columnCount) {
        this.dataFrame = dataFrame;
        this.rowStart = rowStart;
        this.columnStart = columnStart;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public DataFrame getDataFrame() {
        return dataFrame;
    }

    public int getRowStart() {
        return rowStart;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public int getColumnStart() {
        return columnStart;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public Row getRow(int row) {
        this.checkRowIndex(row);
        return this.dataFrame.getRow(this.rowStart + row).view(this.columnStart, this.columnCount);
    }

    public String getCell(final int row, final int column) {
        this.checkRowIndex(row);
        this.checkColumnIndex(column);
        return this.dataFrame.getCell(this.rowStart + row, this.columnStart + column);
    }

    @Override
    public Iterator<Row> iterator() {
        return new DataViewIterator(this);
    }

    private void checkRowIndex(final int index) {
        if (index < 0 || index >= this.rowCount)
            throw new IndexOutOfBoundsException(this.outOfBoundsMsg(index, this.rowCount));
    }

    private void checkColumnIndex(final int index) {
        if (index < 0 || index >= this.columnCount)
            throw new IndexOutOfBoundsException(this.outOfBoundsMsg(index, this.columnCount));
    }

    private String outOfBoundsMsg(final int index, final int count) {
        return "Index: " + index + ", Size: " + count;
    }
}
