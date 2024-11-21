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

        final var model = table.getSheet().getDocument().getModel();
        final var cellValue = value.getValue();

        this.name = name;
        this.value = value;
        this.valueOfValue = (valueOfValue == null)
                ? model.toEntityValue(cellValue).orElse(cellValue)
                : valueOfValue;
    }

    @Override
    public BaseHeader clone() {
        return new MetaKeyValueHeader(this.getTable(), this.getCell(), this.name, this.value, this.valueOfValue);
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
