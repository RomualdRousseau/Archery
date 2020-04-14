package com.github.romualdrousseau.any2json.base;

public class RowGroup {

    public RowGroup(BaseCell cell, int rowNum) {
        this.cell = cell;
        this.rowNum = rowNum;
        this.rowCount = 0;
    }

    public BaseCell getCell() {
        return this.cell;
    }

    public int getRow() {
        return this.rowNum;
    }

    public int getNumberOfRows() {
        return this.rowCount;
    }

    public void incNumberOfRows() {
        this.rowCount++;
    }

    private BaseCell cell;
    private int rowNum;
    private int rowCount;
}
