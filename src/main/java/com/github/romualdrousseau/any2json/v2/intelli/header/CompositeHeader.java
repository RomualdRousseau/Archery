package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.HeaderTag;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.intelli.CompositeTable;
import com.github.romualdrousseau.shuju.DataRow;

public abstract class CompositeHeader extends AbstractHeader {

    public abstract CompositeHeader clone();

    public CompositeHeader(final CompositeTable table, final BaseCell cell) {
        super(table, cell);
    }

    @Override
    public String getEntityString() {
        return this.getCell().getEntityString();
    }

    @Override
    public boolean hasTag() {
        return false;
    }

    @Override
    public HeaderTag getTag() {
        return null;
    }

    @Override
    public DataRow buildTrainingRow(final String tagValue, final Header[] conflicts, final boolean ensureWordsExists) {
        return null;
    }

    public CompositeTable getTable() {
        return (CompositeTable) super.getTable();
    }
}
