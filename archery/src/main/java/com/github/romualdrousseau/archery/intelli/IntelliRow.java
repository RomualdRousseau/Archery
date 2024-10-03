package com.github.romualdrousseau.archery.intelli;

import com.github.romualdrousseau.archery.base.BaseRow;
import com.github.romualdrousseau.archery.base.BaseTable;
import com.github.romualdrousseau.archery.commons.collections.Row;

public class IntelliRow extends BaseRow {

    public IntelliRow(final BaseTable table, final int rowIndex, final Row row) {
        super(table, rowIndex);
        this.row = row;
    }

    @Override
    protected String getCellValueAt(final int colIndex) {
        return this.row.get(colIndex);
    }

    @Override
    protected int getNumberOfMergedCellsAt(final int colIndex) {
        return 1;
    }

    private final Row row;
}
