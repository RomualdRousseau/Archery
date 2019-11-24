package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;

public class MetaTableHeader extends MetaHeader {

    public MetaTableHeader(final AbstractTable table, final AbstractCell cell) {
        super(table, cell);
    }

    public MetaTableHeader(final MetaTableHeader parent) {
        super(parent.getTable(), parent.getCell());
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public AbstractCell getCell(final AbstractRow row) {
        return new AbstractCell(super.getValue(), 0, 1, this.getTable().getClassifier());
    }

    @Override
    public AbstractHeader clone() {
        return new MetaTableHeader(this);
    }
}
