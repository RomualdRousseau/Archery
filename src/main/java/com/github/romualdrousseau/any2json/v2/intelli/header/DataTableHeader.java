package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.intelli.CompositeTable;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;

public class DataTableHeader extends CompositeHeader {

    public DataTableHeader(final CompositeTable table, final BaseCell cell) {
        super(table, cell);
    }

    private DataTableHeader(final DataTableHeader parent) {
        this(parent.getTable(), parent.getCell());
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
    public CompositeHeader clone() {
        return new DataTableHeader(this);
    }

    private String name;
}
