package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.Cell;
import com.github.romualdrousseau.any2json.v2.base.Header;

public class MetaHeader extends Header {

    public MetaHeader(Cell cell) {
        super(cell, -1);
    }

    @Override
    public String getName() {
        if (this.name == null) {
            String v1 = this.getCell().getValue();
            String v2 = this.getCell().getClassifier().getEntityList().anonymize(v1);
            this.name = this.getCell().getClassifier().getStopWordList().removeStopWords(v2);
        }
        return this.name;
    }

    @Override
    public String getValue() {
        if (this.value == null) {
            String v1 = this.getCell().getValue();
            String v2 = this.getCell().getClassifier().getEntityList().find(v1);
            this.value = (v2 == null) ? v1 : v2;
        }
        return this.value;
    }

    @Override
    public Header clone() {
        return new MetaHeader(this.getCell());
    }

    private String name;
    private String value;
}
