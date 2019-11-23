package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.base.TableLexer;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;

public class MetaTable extends AbstractTable {

    public MetaTable(AbstractTable table) {
        super(table);
        this.buildSimpleMeta(table);
    }

    public MetaTable(AbstractTable table, LayexMatcher layex) {
        super(table);
        layex.match(new TableLexer(table), new MetaTableContext(this));
    }

    private void buildSimpleMeta(AbstractTable table) {
        for (Row row : table.rows()) {
            for (Cell cell : row.cells()) {
                if(cell.hasValue()) {
                    this.addHeader(new MetaHeader((AbstractCell) cell));
                }
            }
        }
    }
}
