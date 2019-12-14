package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.layex.Context;

public class TableSplitContext extends Context<BaseCell> {

    public static final int TABLE_META = 0;
    public static final int TABLE_HEADER = 1;
    public static final int TABLE_GROUP = 3;
    public static final int TABLE_DATA = 4;
    public static final int TABLE_FOOTER = 6;

    public TableSplitContext() {
        this.splitRows = new ArrayList<Integer>();
    }

    public void processSymbolFunc(final BaseCell cell) {
        if (this.getGroup() == TABLE_FOOTER && cell.getSymbol().equals("$")) {
            this.splitRows.add(this.getRow());
        }
    }

    public List<Integer> getSplitRows() {
        return this.splitRows;
    }

    private ArrayList<Integer> splitRows;
}
