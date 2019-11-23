package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;

public class MetaKeyValueHeader extends MetaHeader {

    public MetaKeyValueHeader(AbstractCell key, AbstractCell value) {
        super(key);
        this.value = value;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            String v1 = this.getCell().getValue();
            this.name = this.getCell().getClassifier().getStopWordList().removeStopWords(v1);
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value.getValue();
    }

    @Override
    public AbstractHeader clone() {
        return new MetaKeyValueHeader(this.getCell(), this.value);
    }

    private AbstractCell value;
    private String name;
}
