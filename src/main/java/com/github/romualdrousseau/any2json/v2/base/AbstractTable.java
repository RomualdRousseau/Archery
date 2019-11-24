package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.Table;

import java.util.HashMap;
import java.util.LinkedList;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.util.RowIterable;
import com.github.romualdrousseau.any2json.v2.util.RowStore;
import com.github.romualdrousseau.any2json.v2.util.Visitable;

public class AbstractTable implements Table, Visitable {

    public AbstractTable(final AbstractSheet sheet, final int firstColumn, final int firstRow, final int lastColumn,
            final int lastRow, final ITagClassifier classifier) {
        assert (firstColumn <= lastColumn) : "fisrt column must be before last column";
        assert (firstRow <= lastRow) : "first row must be before last row";
        this.visited = false;
        this.sheet = sheet;
        this.firstColumn = firstColumn;
        this.firstRow = firstRow;
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
        this.classifier = classifier;
        this.firstRowOffset = 0;
        this.lastRowOffset = 0;
        this.headerRowOffset = 0;
        this.parentOffsetRow = 0;
        this.cachedRows = null;
    }

    public AbstractTable(final ITagClassifier classifier) {
        this(null, 0, 0, 0, 0, classifier);
    }

    public AbstractTable(final AbstractTable parent) {
        this(parent.sheet, parent.firstColumn, parent.firstRow, parent.lastColumn, parent.lastRow, parent.classifier);
        this.cachedRows = parent.cachedRows;
    }

    public AbstractTable(final AbstractTable parent, final int firstRow, final int lastRow) {
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

    public ITagClassifier getClassifier() {
        return this.classifier;
    }

    public int getFirstRowOffset() {
        return this.firstRowOffset;
    }

    public void setFirstRowOffset(final int offset) {
        assert (offset >= 0);
        assert ((this.lastRowOffset - offset) <= 0);
        this.firstRowOffset = offset;
    }

    public int getLastRowOffset() {
        return this.lastRowOffset;
    }

    public void setLastRowOffset(final int offset) {
        assert (offset <= 0);
        assert ((offset - this.firstRowOffset) <= 0);
        this.lastRowOffset = offset;
    }

    public int getHeaderRowOffset() {
        return this.headerRowOffset;
    }

    public void setHeaderRowOffset(final int offset) {
        assert (offset >= 0);
        assert ((this.lastRowOffset - offset) <= 0);
        this.headerRowOffset = offset;
    }

    public AbstractRow getRowAt(final int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        if (this.cachedRows == null) {
            this.cachedRows = new RowStore();
        }

        AbstractRow result = cachedRows.get(this.parentOffsetRow + this.firstRowOffset + rowIndex);
        if (result == null) {
            result = new AbstractRow(this, rowIndex);
            cachedRows.put(rowIndex, result);
        }

        return result;
    }

    public void addHeader(final AbstractHeader header) {
        this.headers.addLast(header);
    }

    public boolean checkIfHeaderExists(final AbstractHeader header) {
        return this.headers.contains(header);
    }

    public AbstractHeader findHeader(final AbstractHeader headerToFind) {
        for (final Header header : this.headers) {
            if (header.equals(headerToFind)) {
                return (AbstractHeader) header;
            }
        }
        return null;
    }

    public void updateHeaderTags() {
        assert(this.classifier != null) : "Classifier must exist";

        for (Header header : this.headers) {
            ((AbstractHeader) header).updateTag(false);
        }

        for (Header header : this.headers) {
            ((AbstractHeader) header).updateTag(true);
        }

        for (Header header : this.headers) {
            if (header.hasTag() && !header.getTag().isUndefined()) {
                Header head = this.headersByTag.putIfAbsent(header.getTag().getValue(), header);
                if (head != null) {
                    ((AbstractHeader) head).mergeTo((AbstractHeader) header);
                }
            }
        }
    }

    private boolean visited;
    private final AbstractSheet sheet;
    private final int firstColumn;
    private final int firstRow;
    private final int lastColumn;
    private final int lastRow;
    private int parentOffsetRow;
    private int firstRowOffset;
    private int lastRowOffset;
    private int headerRowOffset;
    private final ITagClassifier classifier;
    private RowStore cachedRows;
    private final LinkedList<Header> headers = new LinkedList<Header>();
    private final HashMap<String, Header> headersByTag = new HashMap<String, Header>();
}
