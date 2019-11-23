package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.shuju.util.StringUtility;
import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.layex.Symbol;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.shuju.math.Vector;

public class AbstractCell implements Cell, Symbol {

    public final static AbstractCell EndOfRow = new AbstractCell(null, 0, null);

    public final static AbstractCell EndOfStream = new AbstractCell(null, 0, null);

    public AbstractCell(String value, int mergedCount, ITagClassifier classifier) {
        this.value = value;
        this.mergedCount = mergedCount;
        this.classifier = classifier;
    }

    public ITagClassifier getClassifier() {
        return this.classifier;
    }

    @Override
    public boolean hasValue() {
        return !StringUtility.isEmpty(this.value);
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public Cell setValue(String value) {
        this.value = value;
        this.entityVector = null;
        return this;
    }

    @Override
    public Vector getEntityVector() {
        if (this.classifier != null && this.entityVector == null) {
            this.entityVector = this.classifier.getEntityList().word2vec(this.value);
        }
        return this.entityVector;
    }

    @Override
    public int getMergedCount() {
        return this.mergedCount;
    }

    @Override
    public String getSymbol() {
        if(this.symbol == null) {
            if (this == AbstractCell.EndOfStream) {
                this.symbol = "";
            } else if (this == AbstractCell.EndOfRow) {
                this.symbol = "$";
            } else if (!this.hasValue()) {
                this.symbol = "s";
            } else if (this.getEntityVector().sparsity() < 1.0f) {
                this.symbol = "m";
            } else {
                this.symbol = "v";
            }
        }
        return this.symbol;
    }

    private String value;
    private int mergedCount;
    private Vector entityVector;
    private ITagClassifier classifier;
    private String symbol;
}
