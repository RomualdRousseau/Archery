package com.github.romualdrousseau.any2json.intelli;

import com.github.romualdrousseau.any2json.Cell;
import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.intelli.parser.MetaTableParser;
import com.github.romualdrousseau.any2json.layex.TableLexer;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class MetaTable extends CompositeTable {

    public MetaTable(final CompositeTable table) {
        super(table);
        this.buildSimpleMeta(table);
        this.setLoadCompleted(true);
    }

    public MetaTable(final CompositeTable table, final TableMatcher layex) {
        super(table);
        layex.match(new TableLexer(table, 0), new MetaTableParser(this));
    }

    private void buildSimpleMeta(final CompositeTable table) {
        for (final Row row : table.rows()) {
            for (final Cell cell : row.cells()) {
                if(cell.hasValue()) {
                    this.addHeader(new MetaHeader(this, (BaseCell) cell));
                }
            }
        }
    }
}
