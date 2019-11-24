package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.shuju.util.StringUtility;
import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.layex.Symbol;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.shuju.math.Vector;

public class AbstractCell implements Cell, Symbol {

    public final static AbstractCell Empty = new AbstractCell("", 0, 1, null);

    public final static AbstractCell EndOfRow = new AbstractCell("", 0, 0, null);

    public final static AbstractCell EndOfStream = new AbstractCell("", 0, 0, null);

    public AbstractCell(final String value, final int colIndex, final int mergedCount,
            final ITagClassifier classifier) {
        this.value = (value == null) ? "" : value;
        this.colIndex = colIndex;
        this.mergedCount = mergedCount;
        this.classifier = classifier;
    }

    @Override
    public boolean hasValue() {
        return !StringUtility.isFastEmpty(this.value);
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public Vector getEntityVector() {
        if (this.classifier != null && this.entityVector == null) {
            this.entityVector = this.classifier.getEntityList().word2vec(this.value);
        }
        return this.entityVector;
    }

    @Override
    public String getSymbol() {
        if (this.symbol == null) {
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

    public int getMergedCount() {
        return this.mergedCount;
    }

    public int getColumnIndex() {
        return this.colIndex;
    }

    private final String value;
    private final int colIndex;
    private final int mergedCount;
    private Vector entityVector;
    private final ITagClassifier classifier;
    private String symbol;
}
