package com.github.romualdrousseau.any2json;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.romualdrousseau.shuju.util.StringUtility;

public abstract class Table implements ITable {
    public static boolean IsEmpty(ITable table) {
        return table == null || table.getNumberOfHeaders() == 0;
    }

    public void setMetaTableProcessing(boolean b) {
        this.disableMetaTableProcessing = b;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public boolean isMetaTable() {
        return getNumberOfRows() <= 0;
    }

    public int getNumberOfMetaTables() {
        return this.metaTables.size();
    }

    public ITable getMetaTableAt(int tableIndex) {
        return this.metaTables.get(tableIndex);
    }

    public Iterable<ITable> metatables() {
        return this.metaTables;
    }

    public int getNumberOfMetas() {
        return this.firstRow - this.metaRow;
    }

    public IRow getMetaRowAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfMetas()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        return getInternalRowAt(this.metaRow + rowIndex);
    }

    public boolean hasHeaders() {
        return this.headers.size() > 0 && getNumberOfRows() > 0;
    }

    public void clearHeaders() {
        this.headers.clear();
    }

    public int getNumberOfHeaders() {
        return this.headers.size();
    }

    public IHeader getHeaderAt(int colIndex) {
        return this.headers.get(colIndex);
    }

    public IHeader getHeaderByTag(String tagName) {
        if (tagName == null) {
            throw new IllegalArgumentException();
        }

        return this.headersByTag.get(tagName);
    }

    public Iterable<IHeader> headers() {
        return this.headers;
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

    public int getNumberOfColumns() {
        return this.lastColumn - this.firstColumn + 1;
    }

    public int getNumberOfRows() {
        return this.lastRow - this.firstRow - this.offsetRow + 1;
    }

    public TableRow getRowAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        if(!this.disableMetaTableProcessing) {
            if (rowIndex == 0) {
                this.offsetRow = 0; // reset cursor if start from beginning
                this.lastGroupId = this.groupId;
            } else {
                int currentOffset = this.offsetRow;
                skipEmptyRows(DocumentFactory.DEFAULT_RATIO_EMPTINESS, this.firstRow + this.offsetRow + rowIndex);
                if (this.offsetRow > currentOffset) {
                    if (skipDuplicateHeader(this.firstRow + this.offsetRow + rowIndex)) {
                        addMetaTable(this.firstColumn, this.firstRow + currentOffset + rowIndex, this.lastColumn,
                                this.firstRow + this.offsetRow + rowIndex - 2);
                    } else {
                        addMetaTable(this.firstColumn, this.firstRow + currentOffset + rowIndex, this.lastColumn,
                                this.firstRow + this.offsetRow + rowIndex - 1);
                    }
                }
            }
        }

        return getInternalRowAt(this.firstRow + this.offsetRow + rowIndex);
    }

    public Iterable<IRow> rows() {
        return new RowIterable(this);
    }

    public void resetHeaderTags() {
        for (IHeader header : this.headers) {
            header.resetTag();
        }
        this.headersByTag.clear();
        this.tagUpdated = false;
    }

    public void updateHeaderTags(ITagClassifier classifier) {
        assert classifier != null;

        if (this.tagUpdated) {
            return;
        }

        if (Table.IsEmpty(this)) {
            return;
        }

        this.disableMetaTableProcessing = true;

        for (IHeader header : this.headers) {
            header.setTagClassifier(classifier);
        }

        for (IHeader header : this.headers) {
            header.updateTag(false);
        }

        for (IHeader header : this.headers) {
            header.updateTag(true);
        }

        for (IHeader header : this.headers) {
            if (header.hasTag() && !header.getTag().isUndefined()) {
                IHeader head = this.headersByTag.putIfAbsent(header.getTag().getValue(), header);
                if (head != null) {
                    head.chain(header);
                }
            }
        }

        this.disableMetaTableProcessing = false;

        this.tagUpdated = true;

        if (!isMetaTable() && !checkValidity(classifier.getRequiredTagList())) {
            transformToMetaTable();
            updateHeaderTags(classifier);
        }
    }

    public boolean checkValidity(String[] requiredTagList) {
        if (requiredTagList == null || requiredTagList.length == 0) {
            return true;
        }

        int mask = 0;
        for (IHeader header : this.headers()) {
            for (int j = 0; j < requiredTagList.length; j++) {
                if (header.hasTag() && !header.getTag().isUndefined()
                        && header.getTag().getValue().equals(requiredTagList[j])) {
                    mask |= (1 << j);
                }
            }
        }
        return (mask == ((1 << requiredTagList.length) - 1));
    }

    public void transformToMetaTable() {
        this.firstRow = lastRow + 1; // firstRow represents the first row of data and MetaTable don't have data
        this.offsetRow = 0;

        resetHeaderTags();
        clearHeaders();
        processMetas();
    }

    protected void buildDataTable(int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId) {
        this.metaRow = firstRow;
        this.firstColumn = firstColumn;
        this.firstRow = firstRow + 1; // skip header as we supposed to always have a header
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
        this.groupId = groupId;
        this.lastGroupId = groupId;
        this.offsetRow = 0;

        skipEmptyFirstRows(DocumentFactory.DEFAULT_RATIO_EMPTINESS);

        processHeaders();

        if (isMetaTable()) {
            this.firstRow = this.lastRow + 1;
            processMetas();
        } else {
            addMetaTable(this.firstColumn, this.metaRow, this.lastColumn, this.firstRow - 2); // Do not include the
                                                                                              // headers, hence - 2
        }
    }

    protected void buildMetaTable(int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId) {
        this.metaRow = firstRow;
        this.firstColumn = firstColumn;
        this.firstRow = lastRow + 1; // firstRow represents the first row of data and MetaTable don't have data
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
        this.groupId = groupId;
        this.lastGroupId = groupId;
        this.offsetRow = 0;

        processMetas();
    }

    protected abstract TableRow getInternalRowAt(int rowIndex);

    protected abstract Table createMetaTable(int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId);

    private void processHeaders() {
        this.headers.clear();
        for (TableHeader header : getHeadersAt(this.firstRow - 1)) {
            addHeaderOrMeta(header);
        }
    }

    private void processMetas() {
        this.headers.clear();

        int index = 0;
        for (int j = 0; j < getNumberOfMetas(); j++) {
            IRow metas = getInternalRowAt(this.metaRow + j);
            if (metas == null) {
                continue;
            }

            int ignoreCells = 0;
            for (int i = 0; i < metas.getNumberOfCells(); i++) {
                if (ignoreCells > 0) {
                    ignoreCells--;
                    continue;
                }

                TableMeta meta = new TableMeta().setColumnIndex(index)
                        .setNumberOfCells(metas.getNumberOfMergedCellsAt(i)).setName(metas.getCellValueAt(i))
                        .setTag(null);

                ignoreCells = meta.getNumberOfCells() - 1;

                index++;

                if (!StringUtility.isEmpty(meta.getName())) {
                    addHeaderOrMeta(meta);
                }
            }
        }
    }

    private boolean skipDuplicateHeader(int fromRow) {
        boolean duplicated = true;
        for (TableHeader header : getHeadersAt(fromRow)) {
            if (getHeaderByCleanName(header.getCleanName()) == null) {
                duplicated = false;
            }
        }
        if (duplicated) {
            this.offsetRow++;
        }
        return duplicated;
    }

    private void skipEmptyFirstRows(double ratioOfEmptiness) {
        final int fromRow = this.firstRow - 1;
        final int toRows = Math.min(fromRow + 10, this.lastRow);
        for (int i = fromRow; i <= toRows; i++) {
            if (isEmptyRow(ratioOfEmptiness, i)) {
                this.firstRow++;
            } else {
                return;
            }
        }
    }

    private void skipEmptyRows(double ratioOfEmptiness, int fromRow) {
        final int toRow = Math.min(fromRow + 10, this.lastRow);
        for (int i = fromRow; i <= toRow; i++) {
            if (isEmptyRow(ratioOfEmptiness, i)) {
                this.offsetRow++;
            } else {
                return;
            }
        }
    }

    private boolean isEmptyRow(double ratioOfEmptiness, int rowIndex) {
        TableRow row = getInternalRowAt(rowIndex);
        if (row == null) {
            return true;
        }
        double emptinessFirstCell = Double.valueOf(row.getNumberOfMergedCellsAt(0))
                / Double.valueOf(row.getNumberOfCells());
        return emptinessFirstCell > ratioOfEmptiness || row.isEmpty(ratioOfEmptiness);
    }

    private IHeader getHeaderByCleanName(String cleanName) {
        IHeader result = null;
        for (IHeader header : this.headers) {
            if (header.getCleanName().equals(cleanName)) {
                result = header;
            }
        }
        return result;
    }

    private Iterable<TableHeader> getHeadersAt(int rowIndex) {
        ArrayList<TableHeader> result = new ArrayList<TableHeader>();

        IRow cells = getInternalRowAt(rowIndex);
        if (cells == null) {
            return result;
        }

        int ignoreCells = 0;
        for (int i = 0; i < cells.getNumberOfCells(); i++) {
            if (ignoreCells > 0) {
                ignoreCells--;
                continue;
            }

            TableHeader header = new TableHeader().setColumnIndex(i).setNumberOfCells(cells.getNumberOfMergedCellsAt(i))
                    .setName(cells.getCellValueAt(i)).setTag(null);

            ignoreCells = header.getNumberOfCells() - 1;

            result.add(header);
        }

        return result;
    }

    private void addMetaTable(int firstColumn, int firstRow, int lastColumn, int lastRow) {
        if (disableMetaTableProcessing) {
            return;
        }

        if (firstRow > lastRow) {
            return;
        }

        this.lastGroupId++;

        if (this.metaTables.size() <= this.lastGroupId) { // check if metatable not already processed
            this.metaTables.add(createMetaTable(firstColumn, firstRow, lastColumn, lastRow, this.lastGroupId));
        }
    }

    private ITable addHeaderOrMeta(IHeader header) {
        this.headers.add(header);
        header.setTable(this);
        return this;
    }

    protected ArrayList<IHeader> headers = new ArrayList<IHeader>();
    protected HashMap<String, IHeader> headersByTag = new HashMap<String, IHeader>();
    protected ArrayList<ITable> metaTables = new ArrayList<ITable>();
    protected int metaRow;
    protected int firstColumn;
    protected int firstRow;
    protected int lastColumn;
    protected int lastRow;
    protected int groupId;
    protected int lastGroupId;
    protected int offsetRow;

    private boolean tagUpdated = false;
    private boolean disableMetaTableProcessing = false;
}
