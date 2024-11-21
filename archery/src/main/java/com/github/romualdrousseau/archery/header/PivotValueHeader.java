package com.github.romualdrousseau.archery.header;

public class PivotValueHeader extends PivotKeyHeader {

    public PivotValueHeader(final PivotKeyHeader parent, final String name) {
        super(parent.getTable(), parent.getCell(), parent.getPivotEntityName(), parent.getEntries());
        this.name = name;
    }

    @Override
    public PivotValueHeader clone() {
        return new PivotValueHeader(this, this.name);
    }

    @Override
    public String getName() {
        if(!this.getTable().isLoadCompleted()) {
            return this.getTable().getSheet().getPivotValueFormat();
        } else {
            return String.format(this.getTable().getSheet().getPivotValueFormat(), this.name);
        }
    }

    public PivotValueHeader setName(final String name) {
        this.name = name;
        return this;
    }

    private String name;
}
