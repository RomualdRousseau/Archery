package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;

public class IntelliRow extends BaseRow {

    public IntelliRow(final IntelliTable table, int capacity) {
        super(table, -1);
        this.cellsData = new BaseCell[capacity];
    }

    @Override
    public BaseCell getCellAt(final int colIndex) {
        assert (colIndex >= 0 && colIndex < this.getTable().getNumberOfColumns()) : "column index out of bound";
        final BaseCell cell = this.cellsData[colIndex];
        return (cell == null) ? BaseCell.Empty : cell;
    }

    public void setCellValue(final int colIndex, final String value) {
        if(value != null) {
            this.setCell(colIndex, new BaseCell(value, 0, 1, this.getTable().getClassifier()));
        }
    }

    public void setCell(final int colIndex, final BaseCell cell) {
        assert (colIndex < this.cellsData.length) : "column index out of bound";
        if (cell.hasValue()) {
            if (cell.getMergedCount() == 1) {
                this.cellsData[colIndex] = cell;
            } else {
                this.cellsData[colIndex] = new BaseCell(cell.getValue(), 0, 1, this.getTable().getClassifier());
            }
        }
    }

    private final BaseCell[] cellsData;
}
