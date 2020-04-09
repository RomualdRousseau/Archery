package com.github.romualdrousseau.any2json.intelli.header;

import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.RowGroup;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;

public class MetaTableHeader extends MetaHeader {

    public MetaTableHeader(final CompositeTable table, final BaseCell cell) {
        super(table, cell);
        this.rowGroup = null;
    }

    private MetaTableHeader(final MetaTableHeader parent) {
        this(parent.getTable(), parent.getCell());
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public CompositeHeader clone() {
        return new MetaTableHeader(this);
    }

    @Override
    public boolean isRowGroupName() {
        return this.rowGroup != null;
    }

	public void assignRowGroup(RowGroup rowGroup) {
        this.rowGroup = rowGroup;
    }

    private RowGroup rowGroup;
}
