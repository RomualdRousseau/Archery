package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliTable;

public class MetaKeyValueHeader extends MetaHeader {

    public MetaKeyValueHeader(final IntelliTable table, final BaseCell key, final BaseCell value) {
        super(table, key);
        this.value = value;
    }

    private MetaKeyValueHeader(final MetaKeyValueHeader parent) {
        super((IntelliTable) parent.getTable(), parent.getCell());
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

    private final BaseCell value;
    private String name;
}
