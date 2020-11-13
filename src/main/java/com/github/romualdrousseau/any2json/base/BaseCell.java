package com.github.romualdrousseau.any2json.base;

import com.github.romualdrousseau.any2json.Cell;
import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.shuju.math.Tensor1D;
import com.github.romualdrousseau.shuju.util.StringUtility;

public class BaseCell implements Cell, Symbol {

    public final static BaseCell Empty = new BaseCell("", 0, 1, null);

    public final static BaseCell EndOfRow = new BaseCell("", 0, 0, null);

    public final static BaseCell EndOfStream = new BaseCell("", 0, 0, null);

    public BaseCell(final String value, final int colIndex, final int mergedCount, final ClassifierFactory classifierFactory) {
        this.value = value;
        this.colIndex = colIndex;
        this.mergedCount = mergedCount;
        this.rawValue = value;
        this.classifierFactory = classifierFactory;
    }

    public BaseCell(final String value, final int colIndex, final int mergedCount, final String rawValue, final ClassifierFactory classifierFactory) {
        this.value = value;
        this.colIndex = colIndex;
        this.mergedCount = mergedCount;
        this.rawValue = rawValue;
        this.classifierFactory = classifierFactory;
    }

    @Override
    public boolean hasValue() {
        return !StringUtility.isFastEmpty(this.value);
    }

    @Override
    public String getValue() {
        return (this.value == null) ? "" : this.value;
    }

    @Override
    public String getEntityString() {
        if(classifierFactory == null || !classifierFactory.getLayoutClassifier().isPresent()) {
            return null;
        } else {
            Tensor1D v = this.getEntityVector();
            if(v.sparsity() < 1.0f) {
                return classifierFactory.getLayoutClassifier().get().getEntityList().get(v.argmax());
            } else {
                return null;
            }
        }
    }

    @Override
    public String getSymbol() {
        if (this.symbol == null) {
            if (this == BaseCell.EndOfStream) {
                this.symbol = "";
            } else if (this == BaseCell.EndOfRow) {
                this.symbol = "$";
            } else if (!this.hasValue()) {
                this.symbol = "s";
            } else if (this.getEntityVector().sparsity() < 1.0f) {
                this.symbol = "e";
            } else {
                this.symbol = "v";
            }
        }
        return this.symbol;
    }

    public String getRawValue() {
        return (this.rawValue == null) ? "" : this.rawValue;
    }

    public int getMergedCount() {
        return this.mergedCount;
    }

    public int getColumnIndex() {
        return this.colIndex;
    }

    public Tensor1D getEntityVector() {
        if (this.entityVector == null) {
            if(classifierFactory == null || !classifierFactory.getLayoutClassifier().isPresent()) {
                this.entityVector = Tensor1D.Null;
            } else {
                this.entityVector = classifierFactory.getLayoutClassifier().get().getEntityList().word2vec(this.value);
            }
        }
        return this.entityVector;
    }

    private final String value;
    private final int colIndex;
    private final int mergedCount;
    private final String rawValue;
    private Tensor1D entityVector;
    private String symbol;
    private ClassifierFactory classifierFactory;
}
