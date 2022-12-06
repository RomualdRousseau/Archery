package com.github.romualdrousseau.any2json.intelli.header;

import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;

public class MetaKeyValueHeader extends MetaHeader {

    public MetaKeyValueHeader(final CompositeTable table, final BaseCell key, final BaseCell value) {
        super(table, key);
        this.value = value;
    }

    private MetaKeyValueHeader(final MetaKeyValueHeader parent) {
        this(parent.getTable(), parent.getCell(), parent.value);
    }

    @Override
    public String getName() {
        if (this.name == null) {
            final String v1 = this.getCell().getValue();
            this.name = this.getTable().getSheet().getClassifierFactory().getLayoutClassifier().get().getStopWordList().removeStopWords(v1);
        }
        return this.name;
    }

    @Override
    public String getValue() {
        if (this.valueOfValue == null) {
            final String v1 = this.value.getValue();
            final String v2 = this.getTable().getSheet().getClassifierFactory().getLayoutClassifier().get().getEntityList().find(v1);
            this.valueOfValue = (v2 == null) ? v1 : v2;
        }
        return this.valueOfValue;
    }

    @Override
    public BaseCell getCellAtRow(final Row row) {
        return this.value;
    }

    @Override
    public String getMainEntityAsString() {
        return this.value.getMainEntityAsString();
    }

    @Override
    public CompositeHeader clone() {
        return new MetaKeyValueHeader(this);
    }

    private String name;
    private final BaseCell value;
    private String valueOfValue;
}
