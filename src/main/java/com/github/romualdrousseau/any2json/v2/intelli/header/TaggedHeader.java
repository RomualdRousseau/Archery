package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.Header;
import com.github.romualdrousseau.any2json.v2.base.Cell;

public class TaggedHeader extends Header {

    public TaggedHeader(Cell cell, int colIndex) {
        super(cell, colIndex);
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
        return "#VALUE?";
    }

    @Override
    public Header clone() {
        return new TaggedHeader(this.getCell(), this.getColumnIndex());
    }

    private String name;
}
