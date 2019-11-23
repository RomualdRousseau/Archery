package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;

public class TaggedHeader extends AbstractHeader {

    public TaggedHeader(AbstractCell cell) {
        super(cell);
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
        return null;
    }

    @Override
    public AbstractHeader clone() {
        return new TaggedHeader(this.getCell());
    }

    private String name;
}
