package com.github.romualdrousseau.archery.header;

public class PivotValueHeader extends PivotKeyHeader {

    public PivotValueHeader(final PivotKeyHeader parent, final String name) {
        super(parent.getTable(), parent.getCell());
        this.name = name;
    }

    protected PivotValueHeader(final PivotValueHeader parent) {
        this(parent, parent.name);
    }

    @Override
    public PivotValueHeader clone() {
        return new PivotValueHeader(this);
    }

    @Override
    public String getName() {
        if(!this.getTable().isLoadCompleted()) {
            return this.getTable().getSheet().getPivotValueFormat();
        } else {
            return String.format(this.getTable().getSheet().getPivotValueFormat(), this.name);
        }
    }

    private String name;
}
