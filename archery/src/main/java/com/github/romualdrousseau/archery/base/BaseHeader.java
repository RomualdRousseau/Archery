package com.github.romualdrousseau.archery.base;

import java.util.Optional;

import com.github.romualdrousseau.archery.Header;
import com.github.romualdrousseau.archery.Row;

public abstract class BaseHeader implements Header {

    private final BaseCell cell;

    private BaseTable table;
    private int colIndex;
    private boolean columnEmpty;

    public BaseHeader(final BaseTable table, final BaseCell cell) {
        this.table = table;
        this.cell = cell;
        this.colIndex = cell.getColumnIndex();
        this.columnEmpty = false;
    }

    protected BaseHeader(final BaseHeader parent) {
        this.table = parent.table;
        this.cell = parent.cell;
        this.colIndex = parent.colIndex;
        this.columnEmpty = parent.columnEmpty;
    }

    @Override
    public BaseCell getCellAtRow(final Row row) {
        return ((BaseRow) row).getCellAt(this.getColumnIndex());
    }

    @Override
    public BaseCell getCellAtRow(final Row row, final boolean merged) {
        return this.getCellAtRow(row);
    }

    @Override
    public String getEntitiesAsString() {
        return String.join("|", this.entities());
    }

    @Override
    public Iterable<String> entities() {
        return this.getCell().entities();
    }

    @Override
    public boolean isColumnEmpty() {
        return this.columnEmpty;
    }

    @Override
    public boolean isColumnMerged() {
        return false;
    }

    public void setColumnEmpty(final boolean columnEmpty) {
        this.columnEmpty = columnEmpty;
    }

    public int getColumnIndex() {
        return this.colIndex;
    }

    public void setColumnIndex(final int colIndex) {
        this.colIndex = colIndex;
    }

    public BaseTable getTable() {
        return this.table;
    }

    public void setTable(final BaseTable table) {
        this.table = table;
    }

    public BaseCell getCell() {
        return this.cell;
    }

    public boolean hasRowGroup() {
        return false;
    }

    public boolean isPivotKeyHeader() {
        return this.cell.isPivotKeyHeader();
    }

    public boolean isPivotTypeHeader() {
        return this.cell.isPivotTypeHeader();
    }

    public Optional<String> getPivotKeyEntityAsString() {
        return this.cell.getPivotKeyEntityAsString();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof BaseHeader)) {
            return false;
        }
        final var other = (BaseHeader) o;
        return other != null &&  this.getName().equalsIgnoreCase(other.getName());
    }

    public abstract BaseHeader clone();

    public abstract String getValue();
}
