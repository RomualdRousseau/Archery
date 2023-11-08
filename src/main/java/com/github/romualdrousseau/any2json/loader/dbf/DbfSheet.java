package com.github.romualdrousseau.any2json.loader.dbf;

import com.github.romualdrousseau.any2json.base.SheetStore;
import com.github.romualdrousseau.shuju.bigdata.DataFrame;
import com.github.romualdrousseau.shuju.bigdata.Row;

class DbfSheet implements SheetStore {

    public DbfSheet(final String name, final DataFrame rows) {
        this.name = name;
        this.rows = rows;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getLastColumnNum(final int rowIndex) {
        return this.rows.getColumnCount(rowIndex) - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.rows.getRowCount() - 1;
    }

    @Override
    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final String cell = this.getCellAt(colIndex, rowIndex);
        return cell != null && !cell.isEmpty();
    }

    @Override
    public boolean hasCellDecorationAt(final int colIndex, final int rowIndex) {
        return false;
    }

    @Override
    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final String cell = this.getCellAt(colIndex, rowIndex);
        if(cell == null || cell.isEmpty()) {
            return null;
        }
        return cell;
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        return 1;
    }

    @Override
    public void patchCell(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2, final String value, final boolean unmergeAll) {
        final String newValue;
        if (value == null) {
            newValue = this.getCellAt(colIndex1, rowIndex1);
        } else {
            newValue = value;
        }

        if(rowIndex2 >= this.rows.getRowCount()) {
            return;
        }

        final Row row = this.rows.getRow(rowIndex2);

        if(colIndex2 >= row.size()) {
            return;
        }

        row.set(colIndex2, newValue);
    }

    private String getCellAt(final int colIndex, final int rowIndex) {
        if(rowIndex >= this.rows.getRowCount()) {
            return null;
        }

        final Row row = this.rows.getRow(rowIndex);

        if(colIndex >= row.size()) {
            return null;
        }

        return row.get(colIndex);
    }

    private final String name;
    private final DataFrame rows;
}
