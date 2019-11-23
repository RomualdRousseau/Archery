package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.base.TableLexer;
import com.github.romualdrousseau.any2json.v2.intelli.header.TaggedHeader;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;

public class DataTable extends AbstractTable {

    public DataTable(AbstractTable table) {
        super(table);
        this.buildSimpleTable(table);
    }

    public DataTable(AbstractTable table, LayexMatcher layex) {
        super(table);
        layex.match(new TableLexer(table), new DataTableContext(this));
    }

    private void buildSimpleTable(AbstractTable table) {
        for (Cell cell : table.getRowAt(0).cells()) {
            this.addHeader(new TaggedHeader((AbstractCell) cell) );
        }
        this.setFirstRowOffset(1);
    }
}
