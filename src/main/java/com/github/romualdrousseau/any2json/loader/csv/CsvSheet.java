package com.github.romualdrousseau.any2json.loader.csv;

import java.io.Closeable;
import java.io.IOException;

import com.github.romualdrousseau.any2json.base.PatcheableSheetStore;
import com.github.romualdrousseau.shuju.bigdata.DataFrame;
import com.github.romualdrousseau.shuju.bigdata.Row;
import com.github.romualdrousseau.shuju.strings.StringUtils;

class CsvSheet extends PatcheableSheetStore implements Closeable {

    private final String name;
    private final DataFrame rows;

    public CsvSheet(final String name, final DataFrame rows) {
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
        return this.rows.getRow(rowIndex).size() - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.rows.getRowCount() - 1;
    }

    @Override
    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final var patchCell = this.getPatchCell(colIndex, rowIndex);
        if (patchCell != null) {
            return true;
        } else {
            return this.getCellAt(colIndex, rowIndex) != null;
        }
    }

    @Override
    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final var patchCell = this.getPatchCell(colIndex, rowIndex);
        if (patchCell != null) {
            return patchCell;
        } else {
            return StringUtils.cleanToken(this.getCellAt(colIndex, rowIndex));
        }
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        return 1;
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
}
