package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.base.Cell;
import com.github.romualdrousseau.any2json.v2.layex.Context;

public class DataTableContext extends Context<Cell> {
    public boolean hasHeader = false;
    // public boolean firstRow = true;

    public DataTableContext(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public void func(Cell cell) {
        if (getGroup() == 0) {
            if (cell.getSymbol().equals("m")) {
                if (hasHeader) {
                    this.dataTable.addHeader(new PivotHeader(cell, this.getColumn(), this.dataTable.getClassifier()));
                } else {
                    this.dataTable.addHeader(new MetaHeader(cell, this.dataTable.getClassifier()));
                }
            } else if (!cell.getSymbol().equals("$")) {
                this.dataTable.addHeader(new TaggedHeader(cell, this.getColumn(), this.dataTable.getClassifier()));
                hasHeader = true;
            } else {
                this.dataTable.setOffset(this.getRow() + 1);
            }
        }
        /*
         * else if (getGroup() == 1) { if (cell.getSymbol().equals("$")) { if (firstRow)
         * { for (Cell m : metas) {
         * print(this.dataTable.getClassifier().getEntityList().get(m.getEntityVector().
         * argmax()), " "); }
         *
         * for (Node h : headers) { print(h.cell.getCleanValue(), " "); }
         *
         * if (pivots.size() > 0) {
         * print(this.dataTable.getClassifier().getEntityList().get(pivots.get(0).cell.
         * getEntityVector().argmax()), " "); print("QUANTITY"); }
         *
         * println();
         *
         * firstRow = false; }
         *
         * if (pivots.size() > 0) { for (Node p : pivots) { for (Cell m : metas) {
         * print(m.getCleanValue(), " "); }
         *
         * for (Node h : headers) { if (h.col < values.size()) {
         * print(values.get(h.col).getCleanValue(), " "); } }
         *
         * print(p.cell.getCleanValue(), " "); if (p.col < values.size()) {
         * print(values.get(p.col).getCleanValue()); }
         *
         * println(); } } else { for (Cell m : metas) { print(m.getCleanValue(), " "); }
         *
         * for (Node h : headers) { if (h.col < values.size()) {
         * print(values.get(h.col).getCleanValue(), " "); } }
         *
         * println(); }
         *
         * values.clear(); } else { values.add(cell); } }
         */
    }

    private DataTable dataTable;
}
