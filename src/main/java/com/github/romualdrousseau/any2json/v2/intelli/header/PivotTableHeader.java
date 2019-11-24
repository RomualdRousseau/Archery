package com.github.romualdrousseau.any2json.v2.intelli.header;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.shuju.math.Vector;

public class PivotTableHeader extends MetaHeader {

    public PivotTableHeader(final AbstractTable table, final AbstractCell cell) {
        super(table, cell);
        this.entries = new ArrayList<AbstractCell>();
        this.entries.add(cell);
    }

    private PivotTableHeader(final PivotTableHeader parent) {
        super(parent.getTable(), parent.getCell());
        this.entries = parent.entries;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            final String v1 = this.getCell().getValue();
            final String v2 = this.getTable().getClassifier().getEntityList().anonymize(v1);
            this.name = this.getTable().getClassifier().getStopWordList().removeStopWords(v2);
            this.name += " " + DocumentFactory.PIVOT_SUFFIX;
        }
        return this.name;
    }

    @Override
    public AbstractHeader clone() {
        return new PivotTableHeader(this);
    }

    @Override
    protected Vector buildEntityVector() {
        return this.getCell().getEntityVector();
    }

    public List<AbstractCell> getEntries() {
        return this.entries;
    }

    public void addEntry(final AbstractCell entry) {
        this.entries.add(entry);
    }

    public PivotValueHeader getPivotValue() {
        return new PivotValueHeader(this);
    }

    private String name;
    private final ArrayList<AbstractCell> entries;
}
