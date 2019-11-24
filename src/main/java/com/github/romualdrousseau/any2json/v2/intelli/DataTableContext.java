package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaTableHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.PivotTableHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.TaggedHeader;
import com.github.romualdrousseau.any2json.v2.layex.Context;

public class DataTableContext extends Context<AbstractCell> {

    public static final int TABLE_HEADER = 0;
    public static final int TABLE_BODY = 1;
    public static final int TABLE_FOOTER = 2;

    public DataTableContext(final DataTable dataTable) {
        this.dataTable = dataTable;
        this.canStartPivot = false;
        this.footerProcessed = false;
    }

    public void processSymbolFunc(final AbstractCell cell) {
        final String symbol = cell.getSymbol();

        switch (this.getGroup()) {
        case TABLE_HEADER:
            if (symbol.equals("m")) {
                if (this.canStartPivot) {
                    PivotTableHeader foundPivot = null;
                    for (final Header header : this.dataTable.headers()) {
                        if (header instanceof PivotTableHeader) {
                            foundPivot = (PivotTableHeader) header;
                        }
                    }
                    if (foundPivot == null) {
                        this.dataTable.addHeader(new PivotTableHeader(this.dataTable, cell));
                    } else {
                        foundPivot.addEntry(cell);
                    }
                } else {
                    this.dataTable.addHeader(new MetaTableHeader(this.dataTable, cell));
                }
            } else if (symbol.equals("$")) {
                this.dataTable.setFirstRowOffset(this.getRow() + 1);
                this.canStartPivot = false;
            } else {
                this.dataTable.setHeaderRowOffset(this.getRow());
                this.dataTable.addHeader(new TaggedHeader(this.dataTable, cell));
                this.canStartPivot = true;
            }
            break;

        case TABLE_FOOTER:
            if (symbol.equals("$")) {
                if (!this.footerProcessed) {
                    final int n = this.dataTable.getLastRow() - this.dataTable.getFirstRow();
                    this.dataTable.setLastRowOffset(this.getRow() - n - 1);
                    this.footerProcessed = true;
                }
            }
            break;
        }
    }

    private final DataTable dataTable;
    private boolean canStartPivot;
    private boolean footerProcessed;
}
