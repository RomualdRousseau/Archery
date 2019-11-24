package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;

public class MetaHeader extends AbstractHeader {

    public MetaHeader(final AbstractTable table, final AbstractCell cell) {
        super(table, cell);
    }

    private MetaHeader(final MetaHeader parent) {
        super(parent.getTable(), parent.getCell());
    }

    @Override
    public String getName() {
        if (this.name == null) {
            final String v1 = this.getCell().getValue();
            final String v2 = this.getTable().getClassifier().getEntityList().anonymize(v1);
            this.name = this.getTable().getClassifier().getStopWordList().removeStopWords(v2);
        }
        return this.name;
    }

    @Override
    public String getValue() {
        if (this.value == null) {
            final String v1 = this.getCell().getValue();
            final String v2 = this.getTable().getClassifier().getEntityList().find(v1);
            this.value = (v2 == null) ? v1 : v2;
        }
        return this.value;
    }

    @Override
    public AbstractCell getCellForRow(final AbstractRow row) {
        return new AbstractCell(this.getValue(), 0, 1, this.getTable().getClassifier());
    }

    @Override
    public AbstractHeader clone() {
        return new MetaHeader(this);
    }

    private String name;
    private String value;
}
