package com.github.romualdrousseau.any2json.base;

import java.util.LinkedList;
import java.util.List;

import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.util.RowIterable;
import com.github.romualdrousseau.any2json.util.RowStore;
import com.github.romualdrousseau.any2json.util.Visitable;

public abstract class AbstractTable implements Table, Visitable {

    public AbstractTable(final AbstractSheet sheet, final int firstColumn, final int firstRow, final int lastColumn,
            final int lastRow) {
        assert (firstColumn <= lastColumn) : "fisrt column must be before last column";
        assert (firstRow <= lastRow) : "first row must be before last row";
        this.visited = false;
        this.sheet = sheet;
        this.firstColumn = firstColumn;
        this.firstRow = firstRow;
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
        this.firstRowOffset = 0;
        this.lastRowOffset = 0;
        this.headerRowOffset = 0;
        this.cachedRows = null;
        this.loadCompleted = false;
    }

    public AbstractTable(final AbstractTable parent) {
        this(parent.sheet, parent.firstColumn, parent.firstRow, parent.lastColumn, parent.lastRow);
        this.cachedRows = parent.cachedRows;
    }

    public AbstractTable(final AbstractTable parent, final int firstRow, final int lastRow) {
        this(parent.sheet, parent.firstColumn, firstRow, parent.lastColumn, lastRow);
        this.cachedRows = parent.cachedRows;
    }

    @Override
    public int getNumberOfColumns() {
        return this.lastColumn - this.firstColumn + 1;
    }

    @Override
    public int getNumberOfRows() {
        return (this.lastRow + this.lastRowOffset) - (this.firstRow + this.firstRowOffset) + 1;
    }

    @Override
    public Iterable<Row> rows() {
        return new RowIterable(this);
    }

    @Override
    public int getNumberOfHeaders() {
        return this.headers.size();
    }

    @Override
    public Iterable<Header> headers() {
        return this.headers;
    }

    @Override
    public boolean isVisited() {
        return this.visited;
    }

    @Override
    public void setVisited(final boolean flag) {
        this.visited = flag;
    }

    public AbstractSheet getSheet() {
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

    public int getFirstRowOffset() {
        return this.firstRowOffset;
    }

    public void setFirstRowOffset(final int offset) {
        assert (offset >= 0);
        assert (this.lastRowOffset <= offset);
        this.firstRowOffset = offset;
    }

    public int getLastRowOffset() {
        return this.lastRowOffset;
    }

    public void setLastRowOffset(final int offset) {
        assert (offset <= 0);
        assert (offset <= this.firstRowOffset);
        this.lastRowOffset = offset;
    }

    public int getHeaderRowOffset() {
        return this.headerRowOffset;
    }

    public void setHeaderRowOffset(final int offset) {
        assert (offset >= 0);
        assert (this.lastRowOffset <= offset);
        this.headerRowOffset = offset;
    }

    public boolean isLoadCompleted() {
        return this.loadCompleted;
    }

    public void setLoadCompleted(final boolean flag) {
        this.loadCompleted = flag;
    }

    public BaseRow getRowAt(final int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        if (this.cachedRows == null) {
            this.cachedRows = new RowStore();
        }

        final int relRowIndex = this.firstRowOffset + rowIndex;

        BaseRow result = cachedRows.get(this.firstRow + relRowIndex);
        if (result == null) {
            result = new BaseRow(this, relRowIndex);
            cachedRows.put(this.firstRow + relRowIndex, result);
        }

        return result;
    }

    public void addHeader(final AbstractHeader header) {
        this.headers.addLast(header);
    }

    public void setHeader(final int i, final AbstractHeader header) {
        this.headers.set(i, header);
    }

    public List<Header> findHeader(final Header headerToFind) {
        final LinkedList<Header> result = new LinkedList<Header>();
        for (final Header header : this.headers()) {
            if (header.equals(headerToFind)) {
                result.add(header);
            }
        }
        return result;
    }

    protected void adjustLastRow(int lastRow, boolean withFooter) {
        this.lastRowOffset = withFooter ? -1 : 0;
        this.lastRow = lastRow;
    }

    private boolean visited;
    private final AbstractSheet sheet;
    private final int firstColumn;
    private final int firstRow;
    private final int lastColumn;
    private int lastRow;
    private int firstRowOffset;
    private int lastRowOffset;
    private int headerRowOffset;
    private RowStore cachedRows;
    private final LinkedList<Header> headers = new LinkedList<Header>();
    private boolean loadCompleted;
}
