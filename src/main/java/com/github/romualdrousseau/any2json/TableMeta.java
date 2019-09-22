package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Vector;

public class TableMeta implements IHeader {
    public Vector getWordVector() {
        if (this.classifier != null && this.wordVector == null) {
            this.wordVector = this.classifier.getWordList().word2vec(this.getCleanName());
        }
        return this.wordVector;
    }

    public Vector getEntityVector() {
        if (this.classifier != null && this.entityVector == null) {
            this.entityVector = this.entity2vec();
        }
        return this.entityVector;
    }

    public Vector getConflictVector(boolean checkForConflicts) {
        return new Vector(this.classifier.getWordList().getVectorSize());
    }

    public String getName() {
        return this.name;
    }

    public String getCleanName() {
        if (this.name != null && this.cleanName == null) {
            this.cleanName = this.classifier.getStopWordList()
                    .removeStopWords(this.classifier.getEntityList().anonymize(this.name));
        }
        return this.cleanName;
    }

    public String getValue() {
        String v = this.classifier.getEntityList().find(this.name);
        return (v == null) ? this.name : v;
    }

    public TableMeta setName(String name) {
        this.name = name;
        return this;
    }

    public int getColumnIndex() {
        return this.columnIndex;
    }

    public TableMeta setColumnIndex(int index) {
        this.columnIndex = index;
        return this;
    }

    public int getNumberOfCells() {
        return this.numberOfCells;
    }

    public TableMeta setNumberOfCells(int numberOfCells) {
        this.numberOfCells = numberOfCells;
        return this;
    }

    public boolean hasTag() {
        return this.tag != null;
    }

    public HeaderTag getTag() {
        return this.tag;
    }

    public TableMeta setTag(HeaderTag tag) {
        this.tag = tag;
        return this;
    }

    public ITable getTable() {
        return this.table;
    }

    public TableMeta setTable(ITable table) {
        this.table = table;
        return this;
    }

    public ITagClassifier getTagClassifier() {
        return this.classifier;
    }

    public TableMeta setTagClassifier(ITagClassifier classifier) {
        this.classifier = classifier;
        return this;
    }

    public String toString() {
        return this.name;
    }

    public void resetTag() {
        this.entityVector = null;
        this.tag = null;
    }

    public void updateTag(boolean checkForConflicts) {
        DataRow data = new DataRow().addFeature(this.buildFeature());
        String tagValue = classifier.predict(data);
        this.tag = new HeaderTag(this, tagValue);
    }

    public DataRow buildRow(String tagValue, IHeader[] conflicts, boolean ensureWordsExists) {
        return new DataRow().addFeature(this.buildFeature()).setLabel(this.classifier.getTagList().word2vec(tagValue));
    }

    public void chain(IHeader other) {
        this.next = other;
    }

    public IHeader next() {
        return this.next;
    }

    private Vector buildFeature() {
        final Vector entity2vec = this.getEntityVector();
        final Vector word2vec = this.getWordVector();
        final Vector conflict2vec = this.getConflictVector(false);
        return entity2vec.concat(word2vec).concat(conflict2vec);
    }

    private Vector entity2vec() {
        return this.classifier.getEntityList().word2vec(getName());
    }

    private String name;
    private String cleanName;
    private Vector wordVector;
    private Vector entityVector;
    private int columnIndex;
    private int numberOfCells;
    private ITagClassifier classifier;
    private HeaderTag tag;
    private ITable table;
    private IHeader next = null;
}
