package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.IHeader;
import com.github.romualdrousseau.any2json.v2.ICell;

public class TaggedHeader implements IHeader {

    public TaggedHeader(ICell cell, int colIndex, ITagClassifier classifier) {
        this.cell = cell;
        this.colIndex = colIndex;
        //this.classifier = classifier;
    }

    public String getName() {
        return this.cell.getValue();
    }

    public String getValue() {
        return this.cell.getValue();
    }

    public int getColumnIndex() {
        return this.colIndex;
    }

    private ICell cell;
    private int colIndex;
    //private ITagClassifier classifier;
}
