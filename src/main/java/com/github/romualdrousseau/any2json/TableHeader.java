package com.github.romualdrousseau.any2json;

import java.util.ArrayList;

import com.github.romualdrousseau.shuju.util.StringUtility;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Tensor1D;

public class TableHeader implements IHeader {
    public Tensor1D getWordVector() {
        if (this.classifier != null && this.wordVector == null) {
            this.wordVector = this.classifier.getWordList().word2vec(this.getCleanName());
        }
        return this.wordVector;
    }

    public Tensor1D getEntityVector() {
        if (this.classifier != null && this.entityVector == null) {
            this.entityVector = this.entity2vec(DocumentFactory.DEFAULT_ENTITY_PROBABILITY);
        }
        return this.entityVector;
    }

    public Tensor1D getConflictVector(boolean checkForConflicts) {
        TableHeader[] conflictingHeaders = checkForConflicts ? this.findConflictingHeaders() : null;
        return this.words2vec(conflictingHeaders);
    }

    public String getName() {
        return this.name;
    }

    public String getCleanName() {
        if (this.name != null && this.cleanName == null) {
            if (this.classifier != null) {
                this.cleanName = this.classifier.getStopWordList().removeStopWords(this.name);
            } else {
                this.cleanName = this.name;
            }
        }
        return this.cleanName;
    }

    public String getValue() {
        throw new NotImplementedException("Not implemented");
    }

    public TableHeader setName(String name) {
        this.name = name;
        return this;
    }

    public int getColumnIndex() {
        return this.columnIndex;
    }

    public TableHeader setColumnIndex(int index) {
        this.columnIndex = index;
        return this;
    }

    public int getNumberOfCells() {
        return this.numberOfCells;
    }

    public TableHeader setNumberOfCells(int numberOfCells) {
        this.numberOfCells = numberOfCells;
        return this;
    }

    public boolean hasTag() {
        return this.tag != null;
    }

    public HeaderTag getTag() {
        return this.tag;
    }

    public TableHeader setTag(HeaderTag tag) {
        this.tag = tag;
        return this;
    }

    public ITable getTable() {
        return this.table;
    }

    public TableHeader setTable(ITable table) {
        this.table = table;
        return this;
    }

    public ITagClassifier getTagClassifier() {
        return this.classifier;
    }

    public TableHeader setTagClassifier(ITagClassifier classifier) {
        this.classifier = classifier;
        return this;
    }

    public String toString() {
        return this.name;
    }

    public void resetTag() {
        this.cleanName = null;
        this.wordVector = null;
        this.entityVector = null;
        this.tag = null;
        this.next = null;
    }

    public void updateTag(boolean checkForConflicts) {
        if (StringUtility.isEmpty(this.getCleanName())) {
            this.tag = new HeaderTag(this, "none");
        } else {
            DataRow data = new DataRow().addFeature(this.buildFeature(checkForConflicts));
            String tagValue = classifier.predict(data);
            this.tag = new HeaderTag(this, tagValue);
        }
    }

    public DataRow buildRow(String tagValue, IHeader[] conflicts, boolean ensureWordsExists) {
        if (ensureWordsExists) {
            this.classifier.getWordList().add(this.getCleanName());
            this.wordVector = null;

            if (conflicts != null) {
                for (IHeader conflict : conflicts) {
                    if(conflict instanceof TableHeader) {
                        this.classifier.getWordList().add(conflict.getCleanName());
                        ((TableHeader) conflict).wordVector = null;
                    }
                }
            }
        }
        return new DataRow().addFeature(this.buildFeature(conflicts))
                .setLabel(this.classifier.getTagList().word2vec(tagValue));
    }

    public void chain(IHeader other) {
        this.next = other;
    }

    public IHeader next() {
        return this.next;
    }

    private Tensor1D buildFeature(boolean checkForConflicts) {
        final Tensor1D entity2vec = this.getEntityVector();
        final Tensor1D word2vec = this.getWordVector();
        final Tensor1D conflict2vec = this.getConflictVector(checkForConflicts);
        return entity2vec.concat(word2vec).concat(conflict2vec);
    }

    private Tensor1D buildFeature(IHeader[] conflicts) {
        final Tensor1D entity2vec = this.getEntityVector();
        final Tensor1D word2vec = this.getWordVector();
        final Tensor1D conflict2vec = this.words2vec(conflicts);
        return entity2vec.concat(word2vec).concat(conflict2vec);
    }

    private TableHeader[] findConflictingHeaders() {
        ArrayList<TableHeader> result = new ArrayList<TableHeader>();

        for (IHeader header : this.table.headers()) {
            if (header != this && header instanceof TableHeader && header.hasTag() && !header.getTag().isUndefined()
                    && header.getTag().equals(this.tag)) {
                result.add((TableHeader) header);
            }
        }

        if (result.size() == 0) {
            return null;
        } else {
            return result.toArray(new TableHeader[result.size()]);
        }
    }

    private Tensor1D entity2vec(float p) {
        Tensor1D result = new Tensor1D(this.classifier.getEntityList().getVectorSize());

        int n = 0;
        for (int i = 0; i < Math.min(this.table.getNumberOfRows(), this.classifier.getSampleCount()); i++) {
            IRow row = this.table.getRowAt(i);
            if(row == null) {
                continue;
            }

            TableCell cell = row.getCell(this);
            if (cell.hasValue() && !cell.getEntityVector().isNull()) {
                result.add(cell.getEntityVector());
                n++;
            }
        }

        if (n > 0) {
            result.if_lt_then(p * ((float) n), 0.0f, 1.0f);
        }

        return result;
    }

    private Tensor1D words2vec(IHeader[] headers) {
        Tensor1D result = new Tensor1D(classifier.getWordList().getVectorSize());

        if (headers == null) {
            return result;
        }

        for (IHeader header : headers) {
            result.add(header.getWordVector());
        }

        return result.constrain(0, 1);
    }

    private String name;
    private String cleanName;
    private Tensor1D wordVector;
    private Tensor1D entityVector;
    private int columnIndex;
    private int numberOfCells;
    private ITagClassifier classifier;
    private HeaderTag tag;
    private ITable table;
    private IHeader next = null;
}
