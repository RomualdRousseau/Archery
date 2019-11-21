package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.ICell;
import com.github.romualdrousseau.any2json.v2.IHeader;

public class MetaHeader implements IHeader {

    public MetaHeader(ICell cell, ITagClassifier classifier) {
        this.cell = cell;
        this.classifier = classifier;
    }

    public String getName() {
        if (this.cell.getEntityVector().sparsity() < 1.0f) {
            return this.classifier.getEntityList().get(this.cell.getEntityVector().argmax());
        } else {
            return this.cell.getValue();
        }
    }

    public String getValue() {
        return this.cell.getValue();
    }

    public int getColumnIndex() {
        return -1;
    }

    private ICell cell;
    private ITagClassifier classifier;
}
