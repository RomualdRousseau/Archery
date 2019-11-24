package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;

public class MetaKeyValueHeader extends MetaHeader {

    public MetaKeyValueHeader(final AbstractTable table, final AbstractCell key, final AbstractCell value) {
        super(table, key);
        this.value = value;
    }

    private MetaKeyValueHeader(final MetaKeyValueHeader parent) {
        super(parent.getTable(), parent.getCell());
        this.value = parent.value;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            final String v1 = this.getCell().getValue();
            this.name = this.getTable().getClassifier().getStopWordList().removeStopWords(v1);
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value.getValue();
    }

    @Override
    public AbstractHeader clone() {
        return new MetaKeyValueHeader(this);
    }

    private final AbstractCell value;
    private String name;
}
