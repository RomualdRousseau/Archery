package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.HeaderTag;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;

public class SimpleHeader extends AbstractHeader {

    public SimpleHeader(final AbstractTable table, final AbstractCell cell) {
        super(table, cell);
    }

    private SimpleHeader(final SimpleHeader parent) {
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
    public AbstractCell getCellForRow(final AbstractRow row) {
        return row.getCellAt(this.getColumnIndex());
    }

    @Override
    public AbstractHeader clone() {
        return new SimpleHeader(this);
    }

    private String name;

    @Override
    public boolean hasTag() {
        return false;
    }

    @Override
    public HeaderTag getTag() {
        return null;
    }

    @Override
    public Vector getEntityVector() {
        return null;
    }

    @Override
    public DataRow buildTrainingRow(String tagValue, Header[] conflicts, boolean ensureWordsExists) {
        return null;
    }
}
