package com.github.romualdrousseau.archery.header;

import com.github.romualdrousseau.archery.base.BaseCell;

public class PivotEntry {

    private final BaseCell cell;
    private final String pivotValue;

    private String typeValue;

    public PivotEntry(final BaseCell cell, final String pivotEntityName) {
        this.cell = cell;
        this.pivotValue = cell.getSheet().getDocument().getModel().toEntityValue(cell.getValue(), pivotEntityName)
                .orElse(cell.getValue());
        this.typeValue = cell.getSheet().getDocument().getModel().toEntityName(cell.getValue(), pivotEntityName);
    }

    public BaseCell getCell() {
        return this.cell;
    }

    public String getPivotValue() {
        return this.pivotValue;
    }

    public String getTypeValue() {
        return this.typeValue;
    }

    public PivotEntry setTypeValue(final String typeValue) {
        this.typeValue = typeValue;
        return this;
    }
}
