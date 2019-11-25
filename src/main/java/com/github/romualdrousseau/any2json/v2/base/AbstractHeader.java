package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliTable;

public abstract class AbstractHeader implements Header {

    public abstract AbstractCell getCellForRow(AbstractRow row);

    public abstract AbstractHeader clone();

    public AbstractHeader(final AbstractTable table, final AbstractCell cell) {
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

    public void setTable(final IntelliTable table) {
        this.table = table;
    }

    public AbstractCell getCell() {
        return this.cell;
    }

    public String getCellMergedValue(AbstractRow row) {
		return this.getCellForRow(row).getValue();
	}

    public boolean equals(final AbstractHeader o) {
        return this.getName().equalsIgnoreCase(o.getName());
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof AbstractHeader && this.equals((AbstractHeader) o);
    }

    private AbstractTable table;
    private final AbstractCell cell;
    private int colIndex;
}
