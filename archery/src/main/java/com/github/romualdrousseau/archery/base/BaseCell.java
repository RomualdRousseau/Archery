package com.github.romualdrousseau.archery.base;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import com.github.romualdrousseau.archery.Cell;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;
import com.github.romualdrousseau.archery.commons.types.Tensor;

public class BaseCell implements Cell, Symbol {

    public final static BaseCell Empty = new BaseCell("", 0, 1, null);

    public final static BaseCell EndOfRow = new BaseCell("", 0, 0, null);

    public final static BaseCell EndOfStream = new BaseCell("", 0, 0, null);

    public BaseCell(final String value, BaseCell cell) {
        this(value, cell.getColumnIndex(),
                cell.getMergedCount(), cell.getRawValue(), cell.getSheet());
    }

    public BaseCell(final String value, final int colIndex, final int mergedCount,
            final BaseSheet sheet) {
        this(value, colIndex, mergedCount, value, sheet);
    }

    public BaseCell(final String value, final int colIndex, final int mergedCount, final String rawValue,
            final BaseSheet sheet) {
        this.colIndex = colIndex;
        this.mergedCount = mergedCount;
        this.rawValue = (rawValue == null) ? "" : rawValue;
        this.sheet = sheet;
        this.setValue(value);
    }

    @Override
    public boolean hasValue() {
        return !StringUtils.isFastBlank(this.value);
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
    public List<String> entities() {
        if (this.entityList == null) {
            final var allEntityList = this.sheet.getDocument().getModel().getEntityList();
            final var entityVector = this.getEntityVector();
            this.entityList = IntStream.range(0, entityVector.size)
                    .filter(i -> entityVector.data[i] > 0.0f)
                    .mapToObj(i -> allEntityList.get(i))
                    .toList();
        }
        return this.entityList;
    }

    @Override
    public String getSymbol() {
        if (this == BaseCell.EndOfStream) {
            return "";
        } else if (this == BaseCell.EndOfRow) {
            return "$";
        } else if (!this.hasValue()) {
            return "s";
        } else if (this.entities().size() > 0) {
            return "e";
        } else {
            return "v";
        }
    }

    @Override
    public boolean matchLiteral(final String literal) {
        return this.entities().stream().anyMatch(x -> x.equalsIgnoreCase(literal));
    }

    public BaseSheet getSheet() {
        return this.sheet;
    }

    public String getRawValue() {
        return this.rawValue;
    }

    public int getMergedCount() {
        return this.mergedCount;
    }

    public int getColumnIndex() {
        return this.colIndex;
    }

    public Tensor getEntityVector() {
        if (this.entityVector == null) {
            this.entityVector = this.sheet.getDocument().getModel().toEntityVector(this.value);
        }
        return this.entityVector;
    }

    public boolean isPivotKeyHeader() {
        return this.getPivotKeyEntityAsString().isPresent();
    }

    public boolean isPivotTypeHeader() {
        final var pivotTypeEntityList = this.sheet.getPivotTypeEntityList();
        return pivotTypeEntityList.stream().anyMatch(x -> this.value.matches(x));
    }

    public Optional<String> getPivotKeyEntityAsString() {
        if (this.sheet != null) {
            final var pivotKeyEntityList = this.sheet.getPivotKeyEntityList();
            return this.entities().stream().filter(x -> pivotKeyEntityList.contains(x)).findFirst();
        } else {
            return Optional.empty();
        }
    }

    public void setValue(final String value) {
        this.value = value;
        if (this.sheet != null && this.hasValue()) {
            this.entityVector = null;
            this.entityList = null;
        } else {
            this.entityVector = Tensor.Null;
            this.entityList = Collections.emptyList();
        }
    }

    private final BaseSheet sheet;
    private final int colIndex;
    private final int mergedCount;
    private final String rawValue;

    private String value;
    private Tensor entityVector;
    private List<String> entityList;
}
