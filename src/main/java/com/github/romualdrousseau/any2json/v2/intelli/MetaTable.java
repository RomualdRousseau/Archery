package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.util.TableLexer;

public class MetaTable extends IntelliTable {

    public MetaTable(final IntelliTable table) {
        super(table);
        this.buildSimpleMeta(table);
        this.setLoadCompleted(true);
    }

    public MetaTable(final IntelliTable table, final LayexMatcher layex) {
        super(table);
        layex.match(new TableLexer(table), new MetaTableContext(this));
    }

    private void buildSimpleMeta(final IntelliTable table) {
        for (final Row row : table.rows()) {
            for (final Cell cell : row.cells()) {
                if(cell.hasValue()) {
                    this.addHeader(new MetaHeader(this, (BaseCell) cell));
                }
            }
        }
    }
}
