package com.github.romualdrousseau.archery.header;

import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseTable;
import com.github.romualdrousseau.archery.base.RowGroup;

public class MetaTableHeader extends MetaHeader {

    public MetaTableHeader(final BaseTable table, final BaseCell cell) {
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
    public BaseHeader clone() {
        return new MetaTableHeader(this);
    }

    @Override
    public boolean hasRowGroup() {
        return this.rowGroup != null;
    }

	public void assignRowGroup(final RowGroup rowGroup) {
        this.rowGroup = rowGroup;
    }

    private RowGroup rowGroup;
}
