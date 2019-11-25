package com.github.romualdrousseau.any2json.v2.intelli.header;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliTable;

public class PivotKeyHeader extends MetaHeader {

    public PivotKeyHeader(final IntelliTable table, final BaseCell cell) {
        super(table, cell);
        this.entries = new ArrayList<BaseCell>();
        this.entries.add(cell);
    }

    private PivotKeyHeader(final PivotKeyHeader parent) {
        super((IntelliTable) parent.getTable(), parent.getCell());
        this.entries = parent.entries;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            final String v1 = this.getCell().getValue();
            final String v2 = this.getTable().getClassifier().getEntityList().anonymize(v1);
            this.name = this.getTable().getClassifier().getStopWordList().removeStopWords(v2);
        }
        return this.name + " " + DocumentFactory.PIVOT_SUFFIX;
    }

    @Override
    public AbstractHeader clone() {
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

    private String name;
    private final ArrayList<BaseCell> entries;
}
