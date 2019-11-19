package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.ICell;
import com.github.romualdrousseau.any2json.v2.IRow;
import com.github.romualdrousseau.any2json.v2.ITable;

import java.util.HashMap;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.RowIterable;
import com.github.romualdrousseau.any2json.v2.TableIterable;

public class Table implements ITable {

    public Table(Sheet sheet, int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId, ITagClassifier classifier) {
        this.sheet = sheet;
        this.firstColumn = firstColumn;
        this.firstRow = firstRow;
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
        this.classifier = classifier;
    }

    public Sheet getSheet() {
        return this.sheet;
    }

    public int getFirstColumn() {
        return this.firstColumn;
    }

    public int getFirstRow() {
        return this.firstRow;
    }

    public int getLastColumn() {
        return this.lastColumn;
    }

    public int getLastRow() {
        return this.lastRow;
    }

    public Row getRowAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        Row result = cachedRows.get(Integer.valueOf(rowIndex));
        if(result == null) {
            result = new Row(this, rowIndex, this.classifier);
            cachedRows.put(Integer.valueOf(rowIndex), result);
        }

        return result;
    }

    @Override
    public int getNumberOfColumns() {
        return this.lastColumn - this.firstColumn + 1;
    }

    @Override
    public int getNumberOfRows() {
        return this.lastRow - this.firstRow + 1;
    }

    @Override
    public Iterable<IRow> rows() {
        return new RowIterable(this);
    }

    @Override
    public Iterable<ICell> cells() {
        return new TableIterable(this);
    }

    private Sheet sheet;
    private int firstColumn;
    private int firstRow;
    private int lastColumn;
    private int lastRow;
    private ITagClassifier classifier;
    private HashMap<Integer, Row> cachedRows = new HashMap<Integer, Row>();
}
