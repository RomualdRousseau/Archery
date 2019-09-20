package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.util.StringUtility;
import com.github.romualdrousseau.shuju.math.Vector;

public class TableCell {
    public TableCell(IHeader header) {
        this.header = header;
        this.classifier = header.getTagClassifier();
    }

    public IHeader getHeader() {
        return this.header;
    }

    public boolean hasValue() {
        return !StringUtility.isEmpty(value);
    }

    public String getValue() {
        return this.value;
    }

    public String getCleanValue() {
        if (value != null && this.cleanValue == null) {
            this.cleanValue = StringUtility.cleanToken(value);
        }
        return this.cleanValue;
    }

    public Vector getEntityVector() {
        if (this.classifier != null && this.entityVector == null) {
            this.entityVector = this.classifier.getEntityList().word2vec(this.getCleanValue());
        }
        return this.entityVector;
    }

    public TableCell setValue(String value) {
        this.value = value;
        return this;
    }

    private String value;
    private String cleanValue;
    private Vector entityVector;
    private IHeader header;
    private ITagClassifier classifier;
}
