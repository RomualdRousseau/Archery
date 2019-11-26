package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.Header;

public class SimpleTable extends AbstractTable {

    public SimpleTable(AbstractSheet sheet, int firstColumn, int firstRow, int lastColumn, int lastRow) {
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

	private void buildSimpleTable(AbstractTable table) {
        for (Cell cell : table.getRowAt(0).cells()) {
            this.addHeader(new SimpleHeader(this, (BaseCell) cell));
        }
        this.setFirstRowOffset(1);
    }
}
