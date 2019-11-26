package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.Header;

public abstract class AbstractHeader implements Header {

    public abstract BaseCell getCellForRow(BaseRow row);

    public AbstractHeader(final AbstractTable table, final BaseCell cell) {
        this.table = table;
        this.cell = cell;
        this.colIndex = cell.getColumnIndex();
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

    public String getCellMergedValue(final BaseRow row) {
		return this.getCellForRow(row).getValue();
    }

    public boolean isRowGroupName() {
        return false;
    }

    public boolean equals(final AbstractHeader o) {
        return this.getName().equalsIgnoreCase(o.getName());
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof AbstractHeader && this.equals((AbstractHeader) o);
    }

    private AbstractTable table;
    private final BaseCell cell;
    private int colIndex;
}
