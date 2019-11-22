package com.github.romualdrousseau.any2json.v2.intelli.header;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.base.Cell;
import com.github.romualdrousseau.any2json.v2.base.Header;

public class PivotHeader extends MetaHeader {

    public PivotHeader(Cell cell, int colIndex) {
        super(cell);
        this.isPivotalKey = true;
        this.colIndexes.add(colIndex);
    }

    private PivotHeader(Cell cell, List<Integer> colIndexes, boolean isPivotalKey) {
        super(cell);
        this.isPivotalKey = isPivotalKey;
        this.colIndexes.addAll(colIndexes);
    }

    @Override
    public String getName() {
        if (this.name == null) {
            String v1 = this.getCell().getValue();
            String v2 = this.getCell().getClassifier().getEntityList().anonymize(v1);
            this.name = this.getCell().getClassifier().getStopWordList().removeStopWords(v2);
            this.name += " " + DocumentFactory.PIVOT_SUFFIX;
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return this.getCell().getValue();
    }

    @Override
    public Header clone() {
        return new PivotHeader(this.getCell(), colIndexes, true);
    }

    public boolean isKey() {
        return this.isPivotalKey;
    }

    public Header getValueHeader() {
        Cell valueCell = new Cell(DocumentFactory.PIVOT_SUFFIX, 1, this.getCell().getClassifier());
        return new PivotHeader(valueCell, colIndexes, false);
    }

    public List<Integer> getColumnIndexes() {
        return this.colIndexes;
    }

    public void addColumnIndex(int colIndex) {
        this.colIndexes.add(colIndex);
    }

    private String name;
    private boolean isPivotalKey;
    private ArrayList<Integer> colIndexes = new ArrayList<Integer>();
}
