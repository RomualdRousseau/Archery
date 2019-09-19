package com.github.romualdrousseau.any2json;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Table implements ITable {
    public static boolean IsEmpty(ITable table) {
        return table == null || table.getNumberOfHeaders() == 0;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public boolean isMetaTable() {
        return getNumberOfRows() == 0;
    }

    public int getNumberOfMetas() {
        return this.firstRow - this.metaRow;
    }

    public IRow getMetaAt(int i) {
        if (i < 0 || i >= getNumberOfMetas()) {
            throw new ArrayIndexOutOfBoundsException(i);
        }

        return getInternalRowAt(this.metaRow + i);
    }

    public List<ITable> getMetaTables() {
        return this.metaTables;
    }

    public int getFirstColumn() {
        return this.firstColumn;
    }

    public int getFirstRow() {
        return this.firstRow;
    }

    public int getNumberOfColumns() {
        return this.lastColumn - this.firstColumn + 1;
    }

    public int getNumberOfRows() {
        return this.lastRow - this.firstRow - this.offsetRow + 1;
    }

    public Iterable<TableHeader> headers() {
        return this.headers;
    }

    public int getNumberOfHeaders() {
        return this.headers.size();
    }

    public TableHeader getHeaderAt(int colIndex) {
        return this.headers.get(colIndex);
    }

    public TableHeader getHeaderByTag(String tagName) {
        if (tagName == null) {
            throw new IllegalArgumentException();
        }

        return this.headersByTag.get(tagName);
    }

    public TableHeader getHeaderByCleanName(String cleanName) {
        TableHeader result = null;
        for (TableHeader header : this.headers) {
            if (header.getCleanName().equals(cleanName)) {
                result = header;
            }
        }
        return result;
    }

    public boolean hasHeaders() {
        return this.headers.size() > 0 && getNumberOfRows() > 0;
    }

    public void clearHeaders() {
        this.headers.clear();
    }

    public Iterable<IRow> rows() {
        return new RowIterable(this);
    }

    public void resetHeaderTags() {
        for (TableHeader header : this.headers) {
            header.resetTag();
        }
        this.headersByTag.clear();
    }

    public void updateHeaderTags(ITagClassifier classifier) {
        assert classifier != null;

        this.resetHeaderTags();

        if (Table.IsEmpty(this)) {
            return;
        }

        for (TableHeader header : this.headers) {
            header.setTagClassifier(classifier);
        }

        for (TableHeader header : this.headers) {
            header.updateTag(false);
        }

        for (TableHeader header : this.headers) {
            header.updateTag(true);
        }

        for (TableHeader header : this.headers) {
            if (header.hasTag() && !header.getTag().isUndefined()) {
                TableHeader head = this.headersByTag.putIfAbsent(header.getTag().getValue(), header);
                if (head != null) {
                    head.chain(header);
                }
            }
        }
    }

    public TableRow getRowAt(int i) {
        if (i < 0 || i >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(i);
        }

        if (i == 0) {
            this.offsetRow = 0; // reset offset if start from beginning;
        } else {
            int currentOffset = this.offsetRow;
            skipEmptyRows(0.5, this.firstRow + this.offsetRow + i);
            if (this.offsetRow > currentOffset) {
                skipDuplicateHeader(this.firstRow + this.offsetRow + i);
                this.lastGroupId++;
                this.addMetaTable(createMetaTable(this.firstColumn, this.firstRow + currentOffset + i, this.lastColumn,
                        this.firstRow + this.offsetRow + i - 1, this.lastGroupId));
            }
        }

        return getInternalRowAt(this.firstRow + this.offsetRow + i);
    }

    protected void buildTable(int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId) {
        this.metaRow = firstRow;
        this.firstColumn = firstColumn;
        this.firstRow = firstRow + 1; // skip header as we supposed to always have a header
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
        this.groupId = groupId;
        this.lastGroupId = groupId;
        this.offsetRow = 0;

        skipEmptyFirstRows(0.5);

        processHeaders();

        if (!isMetaTable()) {
            this.lastGroupId++;
            addMetaTable(createMetaTable(this.firstColumn, this.metaRow,
                    this.lastColumn, this.firstRow - 1, this.lastGroupId));
        }
    }

    protected ITable addHeader(TableHeader header) {
        this.headers.add(header);
        header.setTable(this);
        return this;
    }

    protected ITable addMetaTable(ITable subtable) {
        this.metaTables.add(subtable);
        return this;
    }

    protected void skipEmptyFirstRows(double ratioOfEmptiness) {
        final int numberOfRows = Math.min(10, this.lastRow);
        for (int i = 0; i < numberOfRows; i++) {
            if (isEmptyRow(ratioOfEmptiness, this.firstRow - 1)) {
                this.firstRow++;
            } else {
                return;
            }
        }
    }

    protected void skipEmptyRows(double ratioOfEmptiness, int fromRow) {
        final int numberOfRows = Math.min(fromRow + 10, this.lastRow);
        for (int i = fromRow; i <= numberOfRows; i++) {
            if (isEmptyRow(ratioOfEmptiness, i)) {
                this.offsetRow++;
            } else {
                return;
            }
        }
    }

    protected boolean isEmptyRow(double ratioOfEmptiness, int i) {
        TableRow row = getInternalRowAt(i);
        if (row != null) {
            double emptinessFirstCell = Double.valueOf(row.getNumberOfMergedCellsAt(0))
                    / Double.valueOf(row.getNumberOfCells());
            if (emptinessFirstCell <= ratioOfEmptiness && !row.isEmpty(ratioOfEmptiness)) {
                return false;
            }
        }
        return true;
    }

    protected void processHeaders() {
        for(TableHeader header: getHeadersAt(this.firstRow - 1)) {
            addHeader(header);
        }
    }

    protected void skipDuplicateHeader(int fromRow) {
        boolean duplicated = true;
        for(TableHeader header: getHeadersAt(fromRow)) {
            if (getHeaderByCleanName(header.getCleanName()) == null) {
                duplicated = false;
            }
        }
        if (duplicated) {
            this.offsetRow++;
        }
    }

    protected abstract TableRow getInternalRowAt(int i);

    protected abstract Table createMetaTable(int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId);

    protected abstract List<TableHeader> getHeadersAt(int i);

    protected ArrayList<TableHeader> headers = new ArrayList<TableHeader>();
    protected HashMap<String, TableHeader> headersByTag = new HashMap<String, TableHeader>();
    protected ArrayList<ITable> metaTables = new ArrayList<ITable>();
    protected int metaRow;
    protected int firstColumn;
    protected int firstRow;
    protected int lastColumn;
    protected int lastRow;
    protected int groupId;
    protected int lastGroupId;
    protected int offsetRow;
}
