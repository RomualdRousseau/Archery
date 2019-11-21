package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.IHeader;
import com.github.romualdrousseau.any2json.v2.IRow;
import com.github.romualdrousseau.any2json.v2.ITable;

import java.util.ArrayList;
import java.util.TreeMap;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.RowIterable;

public class Table implements ITable {

    public Table(Sheet sheet, int firstColumn, int firstRow, int lastColumn, int lastRow, ITagClassifier classifier) {
        this.visited = false;
        this.sheet = sheet;
        this.firstColumn = firstColumn;
        this.firstRow = firstRow;
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
        this.rowOffset = 0;
        this.classifier = classifier;
        this.cachedRows = null;
    }

    public Table(Table parent) {
        this(parent.sheet, parent.firstColumn, parent.firstRow, parent.lastColumn, parent.lastRow, parent.classifier);
        this.cachedRows = parent.cachedRows;
    }

    public Table(Table parent, int firstRow, int lastRow) {
        this(parent.sheet, parent.firstColumn, firstRow, parent.lastColumn, lastRow, parent.classifier);
        this.rowOffset = firstRow - parent.firstRow;
        this.cachedRows = parent.cachedRows;
    }

    public boolean isVisited() {
        return this.visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
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

    public ITagClassifier getClassifier() {
        return this.classifier;
    }

    public void setOffset(int offset) {
        this.rowOffset = offset;
    }

    public Row getRowAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        if(this.cachedRows == null) {
            this.cachedRows = new TreeMap<Integer, Row>();
        }

        Row result = cachedRows.get(Integer.valueOf(this.rowOffset + rowIndex));
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
    public int getNumberOfHeaders() {
        return this.headers.size();
    }

    @Override
    public Iterable<IHeader> headers() {
        return this.headers;
    }

    public void addHeader(IHeader header) {
        this.headers.add(header);
	}

    private boolean visited;
    private Sheet sheet;
    private int firstColumn;
    private int firstRow;
    private int lastColumn;
    private int lastRow;
    private int rowOffset;
    private ITagClassifier classifier;
    private TreeMap<Integer, Row> cachedRows;
    private ArrayList<IHeader> headers = new ArrayList<IHeader>();
}
