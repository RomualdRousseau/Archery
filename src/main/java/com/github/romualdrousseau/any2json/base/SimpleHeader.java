package com.github.romualdrousseau.any2json.base;

import java.util.List;

import com.github.romualdrousseau.any2json.HeaderTag;

public class SimpleHeader extends AbstractHeader {

    public SimpleHeader(final AbstractTable table, final BaseCell cell) {
        super(table, cell);
    }

    public SimpleHeader(final AbstractHeader parent) {
        super(parent.getTable(), parent.getCell());
    }

    @Override
    public String getName() {
        if (this.name == null) {
            this.name = this.getCell().getValue();
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return null;
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
    public String getMainEntityAsString() {
        return null;
    }

    @Override
    public List<String> entities() {
        return null;
    }

    private String name;
}
