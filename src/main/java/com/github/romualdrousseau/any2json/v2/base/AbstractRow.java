package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.util.CellIterable;
import com.github.romualdrousseau.any2json.ITagClassifier;

public class AbstractRow implements Row {

    public AbstractRow(AbstractTable table, int rowIndex) {
        this.table = table;
        this.rowIndex = rowIndex;
        this.classifier = table.getClassifier();
        this.cellCount = 0;
        this.emptyCellCount = 0;
        this.islandCellCount = 0;
        this.cellCountUpdated = false;
        this.cachedCells = new AbstractCell[table.getNumberOfColumns()];
    }

    @Override
    public boolean isEmpty() {
        return (this.sparsity() == 1.0f);
    }

    @Override
    public int getNumberOfCells() {
        if (!this.cellCountUpdated) {
            this.updateCellCount();
            this.cellCountUpdated = true;
        }
        return this.cellCount;
    }

    @Override
    public Iterable<Cell> cells() {
        return new CellIterable(this);
    }

    public AbstractTable getTable() {
        return this.table;
    }

    public ITagClassifier getClassifier() {
        return this.classifier;
    }

    public AbstractCell getCellAt(int colIndex) {
        assert(colIndex >= 0 && colIndex < this.table.getNumberOfColumns());
        AbstractCell result = cachedCells[colIndex];
        if(result == null) {
            result = new AbstractCell(this.getCellValueAt(colIndex), colIndex, this.getNumberOfMergedCellsAt(colIndex), this.classifier);
            cachedCells[colIndex] = result;
        }
        return result;
    }

    public float sparsity() {
        if (this.getNumberOfCells() == 0) {
            return 1.0f;
        } else {
            return Float.valueOf(this.emptyCellCount) / Float.valueOf(this.cellCount);
        }
    }

    public float density() {
		return 1.0f / Float.valueOf(this.islandCellCount);
	}

    private int getNumberOfMergedCellsAt(int colIndex) {
        assert(colIndex >= 0 && colIndex < this.table.getNumberOfColumns());
        int col = this.table.getFirstColumn() + colIndex;
        int row = this.table.getFirstRow() + this.rowIndex;
        return this.table.getSheet().getNumberOfMergedCellsAt(col, row);
    }

    private String getCellValueAt(int colIndex) {
        assert(colIndex >= 0 && colIndex < this.table.getNumberOfColumns());
        int col = this.table.getFirstColumn() + colIndex;
        int row = this.table.getFirstRow() + this.rowIndex;
        return this.table.getSheet().getInternalCellValueAt(col, row);
    }

    private void updateCellCount() {
        int n = 0;
        for (int i = 0; i < this.table.getNumberOfColumns();) {
            AbstractCell cell = this.getCellAt(i);
            if (!cell.hasValue()) {
                this.emptyCellCount++;
                n = 0;
            } else {
                if(n == 0) this.islandCellCount++;
                n++;
            }
            this.cellCount++;
            i += cell.getMergedCount();
        }
    }

    private AbstractTable table;
    private int rowIndex;
    private int cellCount;
    private int emptyCellCount;
    private int islandCellCount;
    private boolean cellCountUpdated;
    private ITagClassifier classifier;
    private AbstractCell[] cachedCells;
}
