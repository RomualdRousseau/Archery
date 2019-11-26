package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;
import com.github.romualdrousseau.any2json.v2.intelli.CompositeTable;
import com.github.romualdrousseau.shuju.math.Vector;

public class MetaHeader extends CompositeHeader {

    public MetaHeader(final CompositeTable table, final BaseCell cell) {
        super(table, cell);
    }

    private MetaHeader(final MetaHeader parent) {
        this(parent.getTable(), parent.getCell());
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
    public BaseCell getCellForRow(final BaseRow row) {
        return new BaseCell(this.getValue(), 0, 1, this.getTable().getClassifier());
    }

    @Override
    public CompositeHeader clone() {
        return new MetaHeader(this);
    }

    @Override
    public Vector buildEntityVector() {
        return this.getCell().getEntityVector();
    }

    private String name;
    private String value;
}
