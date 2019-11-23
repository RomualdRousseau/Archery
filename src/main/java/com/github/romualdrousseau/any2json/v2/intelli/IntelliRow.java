package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;

public class IntelliRow extends AbstractRow {

    public IntelliRow(IntelliTable table) {
        super(table, -1);
        this.cells = new AbstractCell[table.getNumberOfHeaders() + 1];
        this.cellsSize = 0;
    }

    public IntelliRow(IntelliRow parent) {
        super(parent.getTable(), -1);
        this.cells = new AbstractCell[parent.cells.length];
        this.cellsSize = 0;

        for(AbstractCell cell : parent.cells) {
            this.addCell(cell);
        }
    }

    public IntelliRow clone() {
        return new IntelliRow(this);
    }

    @Override
    public AbstractCell getCellAt(int colIndex) {
        if (colIndex < 0 || colIndex >= this.getTable().getNumberOfColumns()) {
            throw new ArrayIndexOutOfBoundsException(colIndex);
        }

        return this.cells[colIndex];
    }

    public void addEmptyCell() {
        this.addCell(AbstractCell.Empty);
    }

    public void addStringCell(String value) {
        this.addCell(new AbstractCell(value, 0, 1, this.getClassifier()));
    }

    public void addCell(AbstractCell cell) {
        assert(this.cellsSize < this.cells.length);
        if(cell.getMergedCount() == 1) {
            this.cells[this.cellsSize++] = cell;
        } else {
            this.cells[this.cellsSize++] = new AbstractCell(cell.getValue(), 0, 1, this.getClassifier());
        }
    }

    private AbstractCell[] cells;
    private int cellsSize;
}
