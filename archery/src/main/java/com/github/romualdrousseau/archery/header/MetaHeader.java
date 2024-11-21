package com.github.romualdrousseau.archery.header;

import java.util.Collections;
import java.util.List;

import com.github.romualdrousseau.archery.HeaderTag;
import com.github.romualdrousseau.archery.Row;
import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseTable;

public class MetaHeader extends BaseHeader {

    private final String name;
    private final BaseCell value;
    private final String valueOfValue;

    public MetaHeader(final BaseTable table, final BaseCell cell) {
        super(table, cell);
        final var model = table.getSheet().getDocument().getModel();
        final var cellValue = cell.getValue();
        this.name = this.getPivotKeyEntityAsString().orElseGet(() -> model.toEntityName(cellValue));
        this.value = model.toEntityValue(cellValue).map(x -> new BaseCell(x, cell)).orElse(cell);
        this.valueOfValue = this.value.getValue();
    }

    protected MetaHeader(final MetaHeader parent) {
        this(parent.getTable(), parent.getCell());
    }

    @Override
    public BaseHeader clone() {
        return new MetaHeader(this);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.valueOfValue;
    }

    @Override
    public BaseCell getCellAtRow(final Row row) {
        return this.value;
    }

    @Override
    public List<String> entities() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasTag() {
        return false;
    }

    @Override
    public HeaderTag getTag() {
        return null;
    }
}
