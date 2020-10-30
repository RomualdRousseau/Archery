package com.github.romualdrousseau.any2json.intelli;

import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.base.AbstractTable;
import com.github.romualdrousseau.any2json.base.AbstractSheet;
import com.github.romualdrousseau.any2json.intelli.header.IntelliHeader;

import java.util.HashMap;

public class CompositeTable extends AbstractTable {

    public CompositeTable() {
        this(null, 0, 0, 0, 0);
    }

    public CompositeTable(final CompositeTable parent) {
        super(parent);
    }

    public CompositeTable(final CompositeTable parent, final int firstRow, final int lastRow) {
        super(parent, firstRow, lastRow);
    }

    public CompositeTable(AbstractSheet sheet, int firstColumn, int firstRow, int lastColumn, int lastRow) {
        super(sheet, firstColumn, firstRow, lastColumn, lastRow);
    }

    @Override
    public void updateHeaderTags() {
        for (Header header : this.headers()) {
            ((IntelliHeader) header).resetTag();
        }

        for (Header header : this.headers()) {
            ((IntelliHeader) header).updateTag();
        }

        for (Header header : this.headers()) {
            if (header.hasTag() && !header.getTag().isUndefined()) {
                Header head = this.headersByTag.putIfAbsent(header.getTag().getValue(), header);
                if (head != null) {
                    ((IntelliHeader) head).mergeTo((IntelliHeader) header);
                }
            }
        }
    }

    @Override
    public int getNumberOfHeaderTags() {
        return this.headersByTag.size();
    }

    @Override
    public Iterable<Header> headerTags() {
        return this.headersByTag.values();
    }

    private final HashMap<String, Header> headersByTag = new HashMap<String, Header>();
}
