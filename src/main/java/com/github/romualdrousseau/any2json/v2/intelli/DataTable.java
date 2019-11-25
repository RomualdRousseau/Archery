package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.intelli.header.SimpleHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.TaggedHeader;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.util.TableLexer;

public class DataTable extends AbstractTable {

    public DataTable(AbstractTable table) {
        super(table);
        this.buildSimpleTable(table);
        this.updateHeaderTags();
        this.setLoadCompleted(true);
    }

    public DataTable(AbstractTable table, LayexMatcher layex) {
        super(table);
        layex.match(new TableLexer(table), new DataTableContext(this));
        this.setLoadCompleted(true);
    }

    @Override
    public void updateHeaderTags() {
    }

    @Override
    public int getNumberOfHeaderTags() {
        return 0;
    }

    @Override
    public Iterable<Header> headerTags() {
        return null;
    }

    private void buildSimpleTable(AbstractTable table) {
        for (Cell cell : table.getRowAt(0).cells()) {
            if (table.getClassifier() == null) {
                this.addHeader(new SimpleHeader(this, (AbstractCell) cell));
            } else {
                this.addHeader(new TaggedHeader(this, (AbstractCell) cell));
            }
        }
        this.setFirstRowOffset(1);
    }
}
