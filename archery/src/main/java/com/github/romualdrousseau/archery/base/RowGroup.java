package com.github.romualdrousseau.archery.base;

public class RowGroup {

    public RowGroup(final int rowNum) {
        this(rowNum, null);
    }

    public RowGroup(final int rowNum, final BaseCell cell) {
        this.rowNum = rowNum;

        this.cell = cell; 
        this.rowCount = 0;
    }

    public BaseCell getCell() {
        return this.cell;
    }

    public void setCell(final BaseCell cell) {
        this.cell = cell;
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

    private final int rowNum;
    
    private BaseCell cell;
    private int rowCount;
}
