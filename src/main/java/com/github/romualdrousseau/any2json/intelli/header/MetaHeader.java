package com.github.romualdrousseau.any2json.intelli.header;

import java.util.List;

import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;

public class MetaHeader extends CompositeHeader {

    public MetaHeader(final CompositeTable table, final BaseCell cell) {
        super(table, cell);

        final String cellValue = this.getCell().getValue();
        this.transformedCell = this.getLayoutClassifier()
                .toEntityValue(cellValue)
                .map(x -> new BaseCell(x, this.getCell()))
                .orElse(this.getCell());
        this.name = this.getPivotEntityAsString().orElseGet(() -> this.getLayoutClassifier().toEntityName(cellValue));
        this.value = this.transformedCell.getValue();
    }

    private MetaHeader(final MetaHeader parent) {
        this(parent.getTable(), parent.getCell());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public BaseCell getCellAtRow(final Row row) {
        return this.transformedCell;
    }

    @Override
    public CompositeHeader clone() {
        return new MetaHeader(this);
    }

    @Override
    public List<String> entities() {
        return null;
    }

    private final String name;
    private final String value;
    private final BaseCell transformedCell;
}
