package com.github.romualdrousseau.any2json.intelli.header;

import com.github.romualdrousseau.any2json.HeaderTag;
import com.github.romualdrousseau.any2json.base.AbstractHeader;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;

public abstract class CompositeHeader extends AbstractHeader {

    public abstract CompositeHeader clone();

    public CompositeHeader(final CompositeTable table, final BaseCell cell) {
        super(table, cell);
    }

    @Override
    public String getMainEntityAsString() {
        return this.getCell().getMainEntityAsString();
    }

    @Override
    public boolean hasTag() {
        return false;
    }

    @Override
    public HeaderTag getTag() {
        return null;
    }

    public CompositeTable getTable() {
        return (CompositeTable) super.getTable();
    }
}
