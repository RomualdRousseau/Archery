package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.intelli.header.IntelliHeader;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.util.TableLexer;

public class DataTable extends IntelliTable {

    public DataTable(IntelliTable table) {
        super(table);
        this.buildIntelliTable(table);
        this.updateHeaderTags();
        this.setLoadCompleted(true);
    }

    public DataTable(IntelliTable table, LayexMatcher layex) {
        super(table);
        layex.match(new TableLexer(table), new DataTableContext(this));
        this.setLoadCompleted(true);
    }

    private void buildIntelliTable(IntelliTable table) {
        for (Cell cell : table.getRowAt(0).cells()) {
            this.addHeader(new IntelliHeader(this, (BaseCell) cell));
        }
        this.setFirstRowOffset(1);
    }
}
