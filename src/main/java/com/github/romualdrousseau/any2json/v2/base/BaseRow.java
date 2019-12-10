package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.util.CellIterable;

public class BaseRow implements Row {

    public BaseRow(final AbstractTable table, final int rowIndex) {
        this.table = table;
        this.rowIndex = rowIndex;
        this.cellCount = 0;
        this.emptyCellCount = 0;
        this.islandCellCount = 0;
        this.cellCountUpdated = false;
        this.cachedCells = new BaseCell[table.getNumberOfColumns()];
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

    @Override
    public BaseCell getCellAt(final int colIndex) {
        if (colIndex < 0 || colIndex >= this.table.getNumberOfColumns()) {
            throw new IndexOutOfBoundsException();
        }
        BaseCell result = cachedCells[colIndex];
        if (result == null) {
            result = new BaseCell(this.getCellValueAt(colIndex), colIndex, this.getNumberOfMergedCellsAt(colIndex),
                    this.table.getClassifier());
            cachedCells[colIndex] = result;
        }
        return result;
    }

    public AbstractTable getTable() {
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
        if (this.getNumberOfCells() == 0) {
            return 0.0f;
        } else {
            return Float.valueOf(this.emptyCellCount) / Float.valueOf(this.cellCount) / Float.valueOf(this.islandCellCount);
        }
    }

    private int getNumberOfMergedCellsAt(final int colIndex) {
        assert (colIndex >= 0 && colIndex < this.table.getNumberOfColumns()) : "column index out of bound";
        final int col = this.table.getFirstColumn() + colIndex;
        final int row = this.table.getFirstRow() + this.rowIndex;
        return this.table.getSheet().getNumberOfMergedCellsAt(col, row);
    }

    private String getCellValueAt(final int colIndex) {
        assert (colIndex >= 0 && colIndex < this.table.getNumberOfColumns()) : "column index out of bound";
        final int col = this.table.getFirstColumn() + colIndex;
        final int row = this.table.getFirstRow() + this.rowIndex;
        return this.table.getSheet().getInternalCellValueAt(col, row);
    }

    private void updateCellCount() {
        int n = 0;
        for (int i = 0; i < this.table.getNumberOfColumns();) {
            final BaseCell cell = this.getCellAt(i);
            if (!cell.hasValue()) {
                this.emptyCellCount++;
                n = 0;
            } else {
                if (n == 0) {
                    this.islandCellCount++;
                }
                n++;
            }
            this.cellCount++;
            i += cell.getMergedCount();
        }
    }

    private final AbstractTable table;
    private final int rowIndex;
    private int cellCount;
    private int emptyCellCount;
    private int islandCellCount;
    private boolean cellCountUpdated;
    private final BaseCell[] cachedCells;
}
