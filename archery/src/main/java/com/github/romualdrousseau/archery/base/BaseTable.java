package com.github.romualdrousseau.archery.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.archery.Header;
import com.github.romualdrousseau.archery.Row;
import com.github.romualdrousseau.archery.Table;

public class BaseTable implements Table, Visitable {

    public BaseTable(final BaseSheet sheet, final int firstColumn, final int firstRow, final int lastColumn,
            final int lastRow) {
        this(sheet, firstColumn, firstRow, lastColumn, lastRow, new RowCache(), new ArrayList<>());
    }

    public BaseTable(final BaseTable parent) {
        this(parent.sheet, parent.firstColumn, parent.firstRow, parent.lastColumn, parent.lastRow, parent.cachedRows,
                parent.ignoreRows);
    }

    public BaseTable(final BaseTable parent, final int firstRow, final int lastRow) {
        this(parent.sheet, parent.firstColumn, firstRow, parent.lastColumn, lastRow, parent.cachedRows,
                parent.ignoreRows);
    }

    private BaseTable(final BaseSheet sheet, final int firstColumn, final int firstRow, final int lastColumn,
            final int lastRow, final RowCache cachedRows, final List<Integer> ignoreRows) {
        assert (firstColumn <= lastColumn) : "first column must be before last column";
        assert (firstRow <= lastRow) : "first row must be before last row";

        this.sheet = sheet;
        this.firstColumn = firstColumn;
        this.lastColumn = lastColumn;
        this.cachedRows = cachedRows;
        this.ignoreRows = ignoreRows;
        this.headers = new ArrayList<>(lastColumn - firstColumn);

        this.visited = false;
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.firstRowOffset = 0;
        this.lastRowOffset = 0;
        this.headerRowOffset = 0;
        this.loadCompleted = false;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public BaseSheet getSheet() {
        return this.sheet;
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
    public BaseRow getRowAt(final int rowIndex) {
        if (rowIndex < 0 || rowIndex >= this.getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }
        final var rowGID = this.firstRow + this.firstRowOffset + rowIndex;
        return this.cachedRows.computeIfAbsent(rowGID, x -> new BaseRow(this, x - this.firstRow));
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
    public List<String> getHeaderNames() {
        final var result = new ArrayList<String>();
        for (final var header : this.headers()) {
            if (!header.isColumnEmpty()) {
                result.add(header.getName());
            }
        }
        return result;
    }

    @Override
    public BaseHeader getHeaderAt(final int i) {
        return (i < this.headers.size()) ? (BaseHeader) this.headers.get(i) : null;
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

    @Override
    public void updateHeaderTags() {
    }

    @Override
    public int getNumberOfHeaderTags() {
        return 0;
    }

    @Override
    public Iterable<Header> headerTags() {
        return null;
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

    public void adjustLastRow(final int lastRow) {
        this.lastRowOffset = -1;
        this.lastRow = lastRow;
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

    public List<Integer> ignoreRows() {
        return this.ignoreRows;
    }

    public void addHeader(final BaseHeader header) {
        this.headers.add(header);
    }

    public void setHeader(final int i, final BaseHeader header) {
        this.headers.set(i, header);
    }

    public List<BaseHeader> findAllHeaders(final BaseHeader headerToFind) {
        final var result = new ArrayList<BaseHeader>();
        for (final var header : this.headers()) {
            if (header.equals(headerToFind)) {
                result.add((BaseHeader) header);
            }
        }
        return result;
    }

    public BaseHeader findHeaderByColumnIndex(final int columnIndex) {
        for (final var header : this.headers()) {
            if (((BaseHeader) header).getColumnIndex() == columnIndex) {
                return (BaseHeader) header;
            }
        }
        return null;
    }

    public BaseHeader findClosestHeader(final BaseHeader headerToFind) {
        for (final var header : this.headers()) {
            if (header.equals(headerToFind)) {
                return (BaseHeader) header;
            }
        }
        return null;
    }

    private final RowCache cachedRows;
    private final List<Integer> ignoreRows;
    private final BaseSheet sheet;
    private final int firstColumn;
    private final int lastColumn;
    private final ArrayList<Header> headers;
    private  int firstRow;
    private int lastRow;
    private int firstRowOffset;
    private int lastRowOffset;
    private int headerRowOffset;
    private boolean loadCompleted;
    private boolean visited;
}
