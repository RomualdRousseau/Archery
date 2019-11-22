package com.github.romualdrousseau.any2json.v2.intelli.header;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.base.Header;
import com.github.romualdrousseau.any2json.v2.ICell;

public class PivotHeader extends Header {

    public PivotHeader(ICell cell, int colIndex, ITagClassifier classifier) {
        super(classifier);
        this.cell = cell;
        this.colIndexes.add(colIndex);
    }

    public String getName() {
        return this.classifier.getEntityList().get(this.cell.getEntityVector().argmax());
    }

    public String getValue() {
        return this.cell.getValue();
    }

    public int getColumnIndex() {
        return this.colIndexes.get(0);
    }

    public List<Integer> getColumnIndexes() {
        return this.colIndexes;
    }

    public void addColumnIndex(int colIndex) {
        this.colIndexes.add(colIndex);
    }

    private ICell cell;
    private ArrayList<Integer> colIndexes = new ArrayList<Integer>();
}
