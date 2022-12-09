package com.github.romualdrousseau.any2json.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.romualdrousseau.any2json.Cell;
import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.any2json.layex.Symbol;
import com.github.romualdrousseau.shuju.math.Tensor1D;
import com.github.romualdrousseau.shuju.nlp.RegexList;
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
    public String getEntitiesAsString() {
        return String.join("|", this.entities());
    }

    @Override
    public Iterable<String> entities() {
        if(classifierFactory == null || !classifierFactory.getLayoutClassifier().isPresent()) {
            return Collections.emptyList();
        } else {
            final List<String> result = new ArrayList<String>();
            final Tensor1D entityVector = this.getEntityVector();
            final RegexList entityList = classifierFactory.getLayoutClassifier().get().getEntityList();
            for (int i = 0; i < entityList.size(); i++) {
                if (entityVector.get(i) == 1) {
                    result.add(entityList.get(i));
                }
            }
            return result;
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
    public boolean matchLiteral(final String literal) {
        for (final String entity: this.entities()) {
            if (entity.equalsIgnoreCase(literal)) {
                return true;
            }
        };
        return false;
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
        return this.getPivotEntityAsString().isPresent();
    }

    public Optional<String> getPivotEntityAsString() {
        if (classifierFactory == null) {
            return Optional.empty();
        }
        return this.classifierFactory.getLayoutClassifier().flatMap(classifier -> {
            final List<String> pivotEntityList = classifier.getPivotEntityList();
            for (int i = 0; i < classifier.getEntityList().size(); i++) {
                if (this.getEntityVector().get(i) > 0) {
                    final String entityString = classifier.getEntityList().get(i);
                    if (pivotEntityList.contains(entityString)) {
                        return Optional.of(entityString);
                    }
                }
            }
            return Optional.empty();
        });
    }

    private final String value;
    private final int colIndex;
    private final int mergedCount;
    private final String rawValue;
    private Tensor1D entityVector;
    private String symbol;
    private final ClassifierFactory classifierFactory;
}
