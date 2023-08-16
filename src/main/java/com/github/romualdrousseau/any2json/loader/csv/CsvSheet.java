package com.github.romualdrousseau.any2json.loader.csv;

import java.io.Closeable;
import java.io.IOException;

import com.github.romualdrousseau.any2json.base.SheetStore;

class CsvSheet implements SheetStore, Closeable {

    private final String name;
    private final MappedRowList<String[]> rows;

    public CsvSheet(final String name, final MappedRowList<String[]> rows) {
        this.name = name;
        this.rows = rows;
    }

    @Override
    public void close() throws IOException {
        this.rows.close();
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getLastColumnNum(final int rowIndex) {
        return this.rows.get(rowIndex).length - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.rows.length() - 1;
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

        if(rowIndex2 >= this.rows.length()) {
            return;
        }

        final String[] row = this.rows.get(rowIndex2);

        if(colIndex2 >= row.length) {
            return;
        }

        row[colIndex2] = newValue;
    }

    private String getCellAt(final int colIndex, final int rowIndex) {
        if(rowIndex >= this.rows.length()) {
            return null;
        }

        final String[] row = this.rows.get(rowIndex);

        if(colIndex >= row.length) {
            return null;
        }

        return row[colIndex];
    }
}
