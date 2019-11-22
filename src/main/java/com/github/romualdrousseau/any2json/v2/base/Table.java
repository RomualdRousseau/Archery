package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.IHeader;
import com.github.romualdrousseau.any2json.v2.IRow;
import com.github.romualdrousseau.any2json.v2.ITable;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.util.RowIterable;
import com.github.romualdrousseau.any2json.v2.util.RowStore;
import com.github.romualdrousseau.any2json.v2.util.Visitable;

public class Table implements ITable, Visitable {

    public Table(Sheet sheet, int firstColumn, int firstRow, int lastColumn, int lastRow, ITagClassifier classifier) {
        this.visited = false;
        this.sheet = sheet;
        this.firstColumn = firstColumn;
        this.firstRow = firstRow;
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
        this.classifier = classifier;
        this.firstOffsetRow = 0;
        this.lastOffsetRow = 0;
        this.parentOffsetRow = 0;
        this.cachedRows = null;
    }

    public Table(Table parent) {
        this(parent.sheet, parent.firstColumn, parent.firstRow, parent.lastColumn, parent.lastRow, parent.classifier);
        this.cachedRows = parent.cachedRows;
    }

    public Table(Table parent, int firstRow, int lastRow) {
        this(parent.sheet, parent.firstColumn, firstRow, parent.lastColumn, lastRow, parent.classifier);
        this.parentOffsetRow = this.firstRow - parent.firstRow;
        this.cachedRows = parent.cachedRows;
    }

    @Override
    public int getNumberOfColumns() {
        return this.lastColumn - this.firstColumn + 1;
    }

    @Override
    public int getNumberOfRows() {
        return (this.lastRow + this.lastOffsetRow) - (this.firstRow + this.firstOffsetRow) + 1;
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

    @Override
    public boolean isVisited() {
        return this.visited;
    }

    @Override
    public void setVisited(boolean flag) {
        this.visited = flag;
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

    public int getFirstOffsetRow() {
        return this.firstOffsetRow;
    }

    public void setFirstOffsetRow(int offset) {
        this.firstOffsetRow = offset;
    }

    public int getLastOffsetRow() {
        return this.lastOffsetRow;
    }

    public void setLastOffsetRow(int offset) {
        this.lastOffsetRow = offset;
    }

    public Row getRowAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        if(this.cachedRows == null) {
            this.cachedRows = new RowStore();
        }

        Row result = cachedRows.get(this.parentOffsetRow + this.firstOffsetRow + rowIndex);
        if(result == null) {
            result = new Row(this, rowIndex, this.classifier);
            cachedRows.put(rowIndex, result);
        }

        return result;
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
    private int parentOffsetRow;
    private int firstOffsetRow;
    private int lastOffsetRow;
    private ITagClassifier classifier;
    private RowStore cachedRows;
    private ArrayList<IHeader> headers = new ArrayList<IHeader>();
}
