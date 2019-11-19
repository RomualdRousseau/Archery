package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.shuju.util.StringUtility;
import com.github.romualdrousseau.any2json.v2.ICell;
import com.github.romualdrousseau.any2json.v2.layex.ISymbol;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.shuju.math.Vector;

public class Cell implements ICell, ISymbol {

    public final static Cell EndOfRow = new Cell(null, 0, null);

    public final static Cell EndOfStream = new Cell(null, 0, null);

    public Cell(String value, int mergedCount, ITagClassifier classifier) {
        this.value = value;
        this.mergedCount = mergedCount;
        this.classifier = classifier;
    }

    @Override
    public boolean hasValue() {
        return !StringUtility.isEmpty(this.value);
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getCleanValue() {
        if (this.value != null && this.cleanValue == null) {
            this.cleanValue = StringUtility.cleanToken(value);
        }
        return this.cleanValue;
    }

    @Override
    public ICell setValue(String value) {
        this.value = value;
        this.cleanValue = null;
        this.entityVector = null;
        return this;
    }

    @Override
    public Vector getEntityVector() {
        if (this.classifier != null && this.entityVector == null) {
            this.entityVector = this.classifier.getEntityList().word2vec(this.getCleanValue());
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
            if (this == Cell.EndOfStream) {
                this.symbol = "";
            } else if (this == Cell.EndOfRow) {
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
    private String cleanValue;
    private int mergedCount;
    private Vector entityVector;
    private ITagClassifier classifier;
    private String symbol;
}
