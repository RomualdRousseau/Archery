package com.github.romualdrousseau.archery.header;

public class PivotTypeHeader extends PivotKeyHeader {

    public PivotTypeHeader(final PivotKeyHeader parent, final String name) {
        super(parent.getTable(), parent.getCell(), parent.getPivotEntityName(), parent.getEntries());
        this.name = name;
    }

    @Override
    public PivotTypeHeader clone() {
        return new PivotTypeHeader(this, this.name);
    }

    @Override
    public String getName() {
        if(!this.getTable().isLoadCompleted()) {
            return this.getTable().getSheet().getPivotTypeFormat();
        } else {
            return String.format(this.getTable().getSheet().getPivotTypeFormat(), this.name);
        }
    }

    public PivotTypeHeader setName(final String name) {
        this.name = name;
        return this;
    }

    private String name;
}
