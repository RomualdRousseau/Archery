package com.github.romualdrousseau.any2json.simple;

import com.github.romualdrousseau.any2json.Cell;
import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.base.BaseTable;
import com.github.romualdrousseau.any2json.simple.header.SimpleHeader;

public class SimpleTable extends BaseTable {

    public SimpleTable(final BaseSheet sheet, final int firstColumn, final int firstRow, final int lastColumn,
            final int lastRow) {
        super(sheet, firstColumn, firstRow, lastColumn, lastRow);
        this.buildSimpleTable(this);
        this.setLoadCompleted(true);
    }

    @Override
    public int getNumberOfHeaderTags() {
        return 0;
    }

    @Override
    public Iterable<Header> headerTags() {
        return null;
    }

    @Override
    public void updateHeaderTags() {
    }

    private void buildSimpleTable(final BaseTable table) {
        for (final Cell cell : table.getRowAt(0).cells()) {
            this.addHeader(new SimpleHeader(this, (BaseCell) cell));
        }
        this.setFirstRowOffset(1);
    }
}
