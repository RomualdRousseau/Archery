package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.ICell;
import com.github.romualdrousseau.any2json.v2.IRow;
import com.github.romualdrousseau.any2json.v2.util.CellIterable;
import com.github.romualdrousseau.any2json.ITagClassifier;

public class Row implements IRow {

    public Row(Table table, int rowIndex, ITagClassifier classifier) {
        this.table = table;
        this.rowIndex = rowIndex;
        this.classifier = classifier;
        this.cellCount = 0;
        this.emptyCellCount = 0;
        this.islandCellCount = 0;
        this.cellCountUpdated = false;
        this.cachedCells = new Cell[table.getNumberOfColumns()];
    }

    public Table getTable() {
        return this.table;
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

    public int getNumberOfMergedCellsAt(int colIndex) {
        if (colIndex < 0 || colIndex >= this.getTable().getNumberOfColumns()) {
            throw new ArrayIndexOutOfBoundsException(colIndex);
        }
        int col = this.getTable().getFirstColumn() + colIndex;
        int row = this.getTable().getFirstRow() + this.rowIndex;
        return this.getTable().getSheet().getNumberOfMergedCellsAt(col, row);
    }

    public String getCellValueAt(int colIndex) {
        if (colIndex < 0 || colIndex >= this.getTable().getNumberOfColumns()) {
            throw new ArrayIndexOutOfBoundsException(colIndex);
        }

        int col = this.getTable().getFirstColumn() + colIndex;
        int row = this.getTable().getFirstRow() + this.rowIndex;
        return this.getTable().getSheet().getInternalCellValueAt(col, row);
    }

    public Cell getCellAt(int colIndex) {
        if (colIndex < 0 || colIndex >= this.getTable().getNumberOfColumns()) {
            throw new ArrayIndexOutOfBoundsException(colIndex);
        }

        Cell result = cachedCells[colIndex];
        if(result == null) {
            result = new Cell(this.getCellValueAt(colIndex), this.getNumberOfMergedCellsAt(colIndex), this.classifier);
            cachedCells[colIndex] = result;
        }

        return result;
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
    public Iterable<ICell> cells() {
        return new CellIterable(this);
    }

    private void updateCellCount() {
        int n = 0;
        for (int i = 0; i < this.getTable().getNumberOfColumns();) {
            Cell cell = this.getCellAt(i);
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

    private Table table;
    private int rowIndex;
    private int cellCount;
    private int emptyCellCount;
    private int islandCellCount;
    private boolean cellCountUpdated;
    private ITagClassifier classifier;
    private Cell[] cachedCells;
}
