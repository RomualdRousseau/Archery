package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;

public class TaggedHeader extends AbstractHeader {

    public TaggedHeader(final AbstractTable table, final AbstractCell cell) {
        super(table, cell);
    }

    private TaggedHeader(final TaggedHeader parent) {
        super(parent.getTable(), parent.getCell());
    }

    @Override
    public String getName() {
        if (this.name == null) {
            final String v1 = this.getCell().getValue();
            this.name = this.getTable().getClassifier().getStopWordList().removeStopWords(v1);
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public AbstractCell getCell(final AbstractRow row) {
        return row.getCellAt(this.getColumnIndex());
    }

    @Override
    public AbstractHeader clone() {
        return new TaggedHeader(this);
    }

    @Override
    protected AbstractHeader[] findConflictingHeaders() {
        return this.findConflictingHeaders(this, this.getTag(), this.getTable().headers());
    }

    private String name;
}
