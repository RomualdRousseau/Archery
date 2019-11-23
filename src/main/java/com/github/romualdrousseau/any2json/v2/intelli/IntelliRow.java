package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;

public class IntelliRow extends AbstractRow {

    public IntelliRow(IntelliTable table) {
        super(table, -1);
        this.cellsData = new AbstractCell[table.getNumberOfHeaders() + 2]; // Account for possible pivot value
        this.cellsIndex = 0;
    }

    @Override
    public AbstractCell getCellAt(int colIndex) {
        assert(colIndex >= 0 && colIndex < this.getTable().getNumberOfColumns());
        AbstractCell cell = this.cellsData[colIndex];
        return (cell == null) ? AbstractCell.Empty : cell;
    }

    public void addEmptyCell() {
        this.addCell(AbstractCell.Empty);
    }

    public void addStringCell(String value) {
        this.addCell(new AbstractCell(value, 0, 1, this.getClassifier()));
    }

    public void addCell(AbstractCell cell) {
        assert(this.cellsIndex < this.cellsData.length);
        if(cell.getMergedCount() == 1) {
            this.cellsData[this.cellsIndex++] = cell;
        } else {
            this.cellsData[this.cellsIndex++] = new AbstractCell(cell.getValue(), 0, 1, this.getClassifier());
        }
    }

    public int getToTo() {
        return cellsIndex;
    }

    private AbstractCell[] cellsData;
    private int cellsIndex;
}
