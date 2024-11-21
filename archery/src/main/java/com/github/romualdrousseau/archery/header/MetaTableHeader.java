package com.github.romualdrousseau.archery.header;

import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseTable;
import com.github.romualdrousseau.archery.base.RowGroup;

public class MetaTableHeader extends MetaHeader {

    private RowGroup rowGroup;

    public MetaTableHeader(final BaseTable table, final BaseCell cell) {
        this(table, cell, null);
    }

    protected MetaTableHeader(final BaseTable table, final BaseCell cell, final RowGroup rowGroup) {
        super(table, cell);
        this.rowGroup = rowGroup;
    }

    @Override
    public BaseHeader clone() {
        return new MetaTableHeader(this.getTable(), this.getCell(), this.rowGroup);
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public boolean hasRowGroup() {
        return this.rowGroup != null;
    }

	public void assignRowGroup(final RowGroup rowGroup) {
        this.rowGroup = rowGroup;
    }
}
