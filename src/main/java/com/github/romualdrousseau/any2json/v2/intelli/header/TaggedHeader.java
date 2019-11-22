package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.base.Header;
import com.github.romualdrousseau.any2json.v2.ICell;

public class TaggedHeader extends Header {

    public TaggedHeader(ICell cell, int colIndex, ITagClassifier classifier) {
        super(classifier);
        this.cell = cell;
        this.colIndex = colIndex;
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
}
