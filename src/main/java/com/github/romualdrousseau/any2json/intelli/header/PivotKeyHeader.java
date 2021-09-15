package com.github.romualdrousseau.any2json.intelli.header;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;

public class PivotKeyHeader extends MetaHeader {

    public PivotKeyHeader(final CompositeTable table, final BaseCell cell) {
        super(table, cell);
        this.entries = new ArrayList<BaseCell>();
        this.entries.add(cell);
    }

    private PivotKeyHeader(final PivotKeyHeader parent) {
        super(parent.getTable(), parent.getCell());
        this.entries = parent.entries;
    }

    @Override
    public String getName() {
        return super.getName() + " " + DocumentFactory.PIVOT_KEY_SUFFIX;
    }

    @Override
    public CompositeHeader clone() {
        return new PivotKeyHeader(this);
    }

    public List<BaseCell> getEntries() {
        return this.entries;
    }

    public void addEntry(final BaseCell entry) {
        this.entries.add(entry);
    }

    public PivotValueHeader getPivotValue() {
        return new PivotValueHeader(this);
    }

    private final ArrayList<BaseCell> entries;
}
