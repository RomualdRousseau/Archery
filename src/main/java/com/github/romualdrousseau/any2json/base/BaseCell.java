package com.github.romualdrousseau.any2json.base;

import java.util.List;

import com.github.romualdrousseau.any2json.Cell;
import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.any2json.layex.Symbol;
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
    public String getMainEntityAsString() {
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

    @Override
    public boolean matchLiteral(String literal) {
        return this.getMainEntityAsString().equals(literal);
    }

    public ClassifierFactory getClassifierFactory() {
        return this.classifierFactory;
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

    public boolean isPivotHeader() {
        return this.getPivotEntityAsString() != null;
    }

    public String getPivotEntityAsString() {
        if (classifierFactory == null || !this.classifierFactory.getLayoutClassifier().isPresent()) {
            return null;
        } else {
            final List<String> pivotEntityList = this.classifierFactory.getLayoutClassifier().get().getPivotEntityList();
            if (pivotEntityList != null) {
                Tensor1D entityVector = this.getEntityVector();
                for (int i = 0; i < this.classifierFactory.getLayoutClassifier().get().getEntityList().size(); i++) {
                    if (entityVector.get(i) == 1) {
                        String entityString = this.classifierFactory.getLayoutClassifier().get().getEntityList().get(i);
                        if (pivotEntityList.contains(entityString)) {
                            return entityString;
                        }
                    }
                }
            }
            return null;
        }
    }

    private final String value;
    private final int colIndex;
    private final int mergedCount;
    private final String rawValue;
    private Tensor1D entityVector;
    private String symbol;
    private ClassifierFactory classifierFactory;
}
