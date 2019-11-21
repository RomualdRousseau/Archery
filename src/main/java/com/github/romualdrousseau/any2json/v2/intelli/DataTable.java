package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.ICell;
import com.github.romualdrousseau.any2json.v2.TableStream;
import com.github.romualdrousseau.any2json.v2.base.Table;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;

public class DataTable extends Table {

    public DataTable(Table table) {
        super(table);
        this.buildSimpleTable(table);
    }

    public DataTable(Table table, LayexMatcher layex) {
        super(table);
        layex.match(new TableStream(table), new DataTableContext(this));
    }

    private void buildSimpleTable(Table table) {
        int colIndex = 0;
        for (ICell cell : table.getRowAt(0).cells()) {
            this.addHeader(new TaggedHeader(cell, colIndex, table.getClassifier()));
            colIndex += cell.getMergedCount();
        }
        this.setOffsetRow(1);
    }
}
