package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.IHeader;
import com.github.romualdrousseau.any2json.v2.base.Cell;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.PivotHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.TaggedHeader;
import com.github.romualdrousseau.any2json.v2.layex.Context;

public class DataTableContext extends Context<Cell> {

    public static final int TABLE_HEADER = 0;
    public static final int TABLE_BODY = 1;
    public static final int TABLE_FOOTER = 2;

    public DataTableContext(DataTable dataTable) {
        this.dataTable = dataTable;
        this.canStartPivot = false;
        this.footerProcessed = false;
    }

    public void processSymbolFunc(Cell cell) {
        String symbol = cell.getSymbol();

        switch (this.getGroup()) {
        case TABLE_HEADER:
            if (symbol.equals("m")) {
                if (this.canStartPivot) {
                    PivotHeader foundPivot = null;
                    for (IHeader header : this.dataTable.headers()) {
                        if (header instanceof PivotHeader) {
                            foundPivot = (PivotHeader) header;
                        }
                    }
                    if (foundPivot == null) {
                        this.dataTable
                                .addHeader(new PivotHeader(cell, this.getColumn(), this.dataTable.getClassifier()));
                    } else {
                        foundPivot.addColumnIndex(this.getColumn());
                    }
                } else {
                    this.dataTable.addHeader(new MetaHeader(cell, this.dataTable.getClassifier()));
                }
            } else if (symbol.equals("$")) {
                this.dataTable.setFirstOffsetRow(this.getRow() - this.dataTable.getFirstRow() + 1);
                this.canStartPivot = false;
            } else {
                this.dataTable.addHeader(new TaggedHeader(cell, this.getColumn(), this.dataTable.getClassifier()));
                this.canStartPivot = true;
            }
            break;

        case TABLE_FOOTER:
            if (symbol.equals("$")) {
                if (!this.footerProcessed) {
                    this.dataTable.setLastOffsetRow(-(this.dataTable.getLastRow() - this.getRow() + 1));
                    this.footerProcessed = true;
                }
            }
            break;
        }
    }

    private DataTable dataTable;
    private boolean canStartPivot;
    private boolean footerProcessed;
}
