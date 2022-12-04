package com.github.romualdrousseau.any2json.intelli;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.Context;
import com.github.romualdrousseau.any2json.base.RowGroup;
import com.github.romualdrousseau.any2json.intelli.header.MetaTableHeader;
import com.github.romualdrousseau.any2json.intelli.header.PivotKeyHeader;
import com.github.romualdrousseau.any2json.intelli.header.DataTableHeader;

public class DataTableContext extends Context<BaseCell> {

    public static final int TABLE_META = 0;
    public static final int TABLE_HEADER = 1;
    public static final int TABLE_GROUP = 3;
    public static final int TABLE_DATA = 4;
    public static final int TABLE_SUB_FOOTER = 5;
    public static final int TABLE_FOOTER = 6;

    public DataTableContext(final DataTable dataTable) {
        this.dataTable = dataTable;
        this.lastRowGroup = null;
        this.firstRowCell = null;
        this.firstRowGroupProcessed = false;
        this.footerProcessed = false;
        this.splitRows = new ArrayList<Integer>();
        this.ignoreRows = new ArrayList<Integer>();
    }

    public void processSymbolFunc(final BaseCell cell) {
        final String symbol = cell.getSymbol();

        if (this.getColumn() == 0) {
            this.firstRowCell = null;
        }
        if (this.firstRowCell == null && cell.hasValue()) {
            this.firstRowCell = cell;
        }

        if (!this.footerProcessed) {
            switch (this.getGroup()) {
            case TABLE_META:
                this.processMeta(cell, symbol);
                break;

            case TABLE_HEADER:
                this.processHeader(cell, symbol);
                break;

            case TABLE_GROUP:
                this.processGroup(cell, symbol);
                break;

            case TABLE_DATA:
                this.processData(cell, symbol);
                break;

            case TABLE_SUB_FOOTER:
                this.processSubFooter(cell, symbol);
                break;

            case TABLE_FOOTER:
                this.processFooter(cell, symbol);
                break;
            }
        } else if (this.getGroup() == TABLE_FOOTER) {
            this.processSplit(cell, symbol);
        }
    }

    public List<Integer> getSplitRows() {
        return this.splitRows;
    }

    public List<Integer> getIgnoreRows() {
        return this.ignoreRows;
    }

    private void processMeta(final BaseCell cell, final String symbol) {
        if (symbol.equals("$")) {
            this.dataTable.setFirstRowOffset(this.getRow() + 1);
        } else if (cell.hasValue()) {
            this.dataTable.addHeader(new MetaTableHeader(this.dataTable, cell));
        }
    }

    private void processHeader(final BaseCell cell, final String symbol) {
        if (symbol.equals("$")) {
            this.dataTable.setFirstRowOffset(this.getRow() + 1);
        } else if (symbol.equals("e") && cell.isPivotHeader()) {
            PivotKeyHeader foundPivot = this.dataTable.findFirstPivotHeader();
            if (foundPivot == null) {
                this.dataTable.addHeader(new PivotKeyHeader(this.dataTable, cell));
            } else {
                foundPivot.addEntry(cell);
            }
        } else {
            this.dataTable.addHeader(new DataTableHeader(this.dataTable, cell));
            this.dataTable.setHeaderRowOffset(this.getRow());
        }
    }

    private void processGroup(final BaseCell cell, final String symbol) {
        if (!symbol.equals("$")) {
            return;
        }

        if (this.getRow() < (this.dataTable.getLastRow() - this.dataTable.getFirstRow())) {
            if (this.firstRowCell != null) {
                this.lastRowGroup = new RowGroup(this.firstRowCell,
                        this.getRow() - this.dataTable.getFirstRowOffset());
                this.dataTable.addRowGroup(this.lastRowGroup);
            }

            if (!this.firstRowGroupProcessed) {
                MetaTableHeader meta = this.dataTable.findFirstMetaTableHeader();
                if (meta == null) {
                    meta = new MetaTableHeader(this.dataTable,
                            new BaseCell("#GROUP?", 0, 1, cell.getRawValue(), this.dataTable.getSheet().getClassifierFactory()));
                    this.dataTable.addHeader(meta);
                }
                meta.assignRowGroup(this.lastRowGroup);
                this.firstRowGroupProcessed = true;
            }
        } else {
            this.processFooter(cell, symbol);
        }
    }

    private void processData(final BaseCell cell, final String symbol) {
        if (!symbol.equals("$")) {
            return;
        }

        if (this.lastRowGroup != null) {
            this.lastRowGroup.incNumberOfRows();
        }
    }

    private void processSubFooter(final BaseCell cell, final String symbol) {
        if (!symbol.equals("$")) {
            return;
        }

        this.ignoreRows.add(this.getRow() - 1);
        this.dataTable.getRowAt(this.getRow() - 1).setIgnored(true);
    }

    private void processFooter(final BaseCell cell, final String symbol) {
        if (!symbol.equals("$")) {
            return;
        }

        final int n = this.dataTable.getLastRow() - this.dataTable.getFirstRow();
        this.dataTable.setLastRowOffset(this.getRow() - n - 1);
        this.splitRows.add(this.getRow() + 1);
        this.footerProcessed = true;
    }

    private void processSplit(final BaseCell cell, final String symbol) {
        if (!symbol.equals("$")) {
            return;
        }

        this.splitRows.add(this.getRow() + 1);
    }

    private final DataTable dataTable;
    private RowGroup lastRowGroup;
    private boolean firstRowGroupProcessed;
    private BaseCell firstRowCell;
    private boolean footerProcessed;
    private final ArrayList<Integer> splitRows;
    private final ArrayList<Integer> ignoreRows;
}
