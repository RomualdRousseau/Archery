package com.github.romualdrousseau.archery.header;

import com.github.romualdrousseau.archery.base.BaseCell;

public class PivotEntry {

    public PivotEntry(final BaseCell cell, final String pivotEntityName) {
        this.cell = cell;
        this.pivotValue = cell.getSheet().getDocument().getModel().toEntityValue(cell.getValue(), pivotEntityName)
                .orElse(cell.getValue());
        this.typeValue = cell.getSheet().getDocument().getModel().toEntityName(cell.getValue(), pivotEntityName);
    }

    private PivotEntry(final PivotEntry pivotEntry) {
        this.cell = pivotEntry.cell;
        this.pivotValue = pivotEntry.pivotValue;
        this.typeValue = pivotEntry.typeValue;
    }

    public BaseCell getCell() {
        return this.cell;
    }

    public String getValue() {
        return this.pivotValue;
    }

    public String getTypeValue() {
        return this.typeValue;
    }

    public PivotEntry setTypeValue(final String typeValue) {
        this.typeValue = typeValue;
        return this;
    }

    public PivotEntry clone() {
        return new PivotEntry(this);
    }

    private final BaseCell cell;
    private final String pivotValue;
    private String typeValue;
}
