package com.github.romualdrousseau.any2json.header;

public class PivotTypeHeader extends PivotKeyHeader {

    public PivotTypeHeader(final PivotKeyHeader parent, final String name) {
        super(parent.getTable(), parent.getCell());
        this.name = name;
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

    @Override
    public PivotTypeHeader clone() {
        return new PivotTypeHeader(this, this.name);
    }

    private String name;
}
