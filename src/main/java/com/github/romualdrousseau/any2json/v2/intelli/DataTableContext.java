package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.base.Cell;
import com.github.romualdrousseau.any2json.v2.layex.Context;

public class DataTableContext extends Context<Cell> {

    public DataTableContext(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public void func(Cell cell) {
        if (getGroup() == 0) {
            if (cell.getSymbol().equals("m")) {
                if (this.hasHeader) {
                    this.dataTable.addHeader(new PivotHeader(cell, this.getColumn(), this.dataTable.getClassifier()));
                } else {
                    this.dataTable.addHeader(new MetaHeader(cell, this.dataTable.getClassifier()));
                }
            } else if (!cell.getSymbol().equals("$")) {
                this.dataTable.addHeader(new TaggedHeader(cell, this.getColumn(), this.dataTable.getClassifier()));
                this.hasHeader = true;
            } else {
                this.dataTable.setOffsetRow(this.getRow() + 1);
            }
        }
    }

    private DataTable dataTable;
    private boolean hasHeader = false;
}
