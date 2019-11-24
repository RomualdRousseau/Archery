package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.util.TableLexer;

public class MetaTable extends AbstractTable {

    public MetaTable(final AbstractTable table) {
        super(table);
        this.buildSimpleMeta(table);
    }

    public MetaTable(final AbstractTable table, final LayexMatcher layex) {
        super(table);
        layex.match(new TableLexer(table), new MetaTableContext(this));
    }

    private void buildSimpleMeta(final AbstractTable table) {
        for (final Row row : table.rows()) {
            for (final Cell cell : row.cells()) {
                if(cell.hasValue()) {
                    this.addHeader(new MetaHeader(this, (AbstractCell) cell));
                }
            }
        }
    }
}
