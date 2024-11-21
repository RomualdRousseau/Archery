package com.github.romualdrousseau.archery.header;

import com.github.romualdrousseau.archery.Row;
import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseTable;

public class MetaKeyValueHeader extends MetaHeader {

    private final String name;
    private final BaseCell value;
    private final String valueOfValue;

    public MetaKeyValueHeader(final BaseTable table, final BaseCell key, final BaseCell value) {
        this(table, key, key.getValue(), value, null);
    }

    protected MetaKeyValueHeader(final BaseTable table, final BaseCell key, final String name, final BaseCell value,
            final String valueOfValue) {
        super(table, key);
        this.name = name;
        this.value = value;
        this.valueOfValue = (valueOfValue == null)
                ? table.getSheet().getDocument().getModel().toEntityValue(value.getValue()).orElse(value.getValue())
                : valueOfValue;
    }

    protected MetaKeyValueHeader(final MetaKeyValueHeader parent) {
        this(parent.getTable(), parent.getCell(), parent.name, parent.value, parent.valueOfValue);
    }

    @Override
    public BaseHeader clone() {
        return new MetaKeyValueHeader(this);
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
    public String getEntitiesAsString() {
        return this.value.getEntitiesAsString();
    }
}
