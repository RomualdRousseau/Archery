package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;

public class IntelliRow extends AbstractRow {

    public IntelliRow(final IntelliTable table) {
        super(table, -1);
        this.cellsData = new AbstractCell[table.getNumberOfHeaders()];
    }

    @Override
    public AbstractCell getCellAt(final int colIndex) {
        assert (colIndex >= 0 && colIndex < this.getTable().getNumberOfColumns());
        final AbstractCell cell = this.cellsData[colIndex];
        return (cell == null) ? AbstractCell.Empty : cell;
    }

    public void setCellValue(final int colIndex, final String value) {
        if(value != null) {
            this.setCell(colIndex, new AbstractCell(value, 0, 1, this.getTable().getClassifier()));
        }
    }

    public void setCell(final int colIndex, final AbstractCell cell) {
        assert (colIndex < this.cellsData.length);
        if (cell.hasValue()) {
            if (cell.getMergedCount() == 1) {
                this.cellsData[colIndex] = cell;
            } else {
                this.cellsData[colIndex] = new AbstractCell(cell.getValue(), 0, 1, this.getTable().getClassifier());
            }
        }
    }

    private final AbstractCell[] cellsData;
}
