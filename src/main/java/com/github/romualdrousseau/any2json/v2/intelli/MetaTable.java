package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.ICell;
import com.github.romualdrousseau.any2json.v2.IRow;
import com.github.romualdrousseau.any2json.v2.TableStream;
import com.github.romualdrousseau.any2json.v2.base.Table;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;

public class MetaTable extends Table {

    public MetaTable(Table table) {
        super(table);
        this.buildSimpleMeta(table);
    }

    public MetaTable(Table table, LayexMatcher layex) {
        super(table);
        layex.match(new TableStream(table), new MetaTableContext(this));
    }

    private void buildSimpleMeta(Table table) {
        for (IRow row : table.rows()) {
            for (ICell cell : row.cells()) {
                if(cell.hasValue()) {
                    this.addHeader(new MetaHeader(cell, this.getClassifier()));
                }
            }
        }
    }
}
