package com.github.romualdrousseau.any2json.base;

import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.Row;

public abstract class AbstractHeader implements Header {

    public AbstractHeader(final AbstractTable table, final BaseCell cell) {
        this.table = table;
        this.cell = cell;
        this.colIndex = cell.getColumnIndex();
    }

    @Override
    public String getRawName() {
        return this.cell.getRawValue();
    }

    @Override
    public BaseCell getCellAtRow(final Row row) {
        return ((BaseRow)row).getCellAt(this.getColumnIndex());
    }

    @Override
    public BaseCell getCellAtRow(final Row row, final boolean merged) {
		return this.getCellAtRow(row);
    }

    public int getColumnIndex() {
        return this.colIndex;
    }

    public void setColumnIndex(final int colIndex) {
        this.colIndex = colIndex;
    }

    public AbstractTable getTable() {
        return this.table;
    }

    public void setTable(final AbstractTable table) {
        this.table = table;
    }

    public BaseCell getCell() {
        return this.cell;
    }

    public boolean isRowGroupName() {
        return false;
    }

    public boolean isPivotHeader() {
        return this.cell.isPivotHeader();
    }

    public boolean equals(final AbstractHeader o) {
        return this.getName().equalsIgnoreCase(o.getName());
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof AbstractHeader && this.equals((AbstractHeader) o);
    }

    public abstract String getValue();

    private AbstractTable table;
    private final BaseCell cell;
    private int colIndex;
}
