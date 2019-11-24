package com.github.romualdrousseau.any2json.v2.intelli.header;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;

public class PivotTableHeader extends MetaHeader {

    public PivotTableHeader(final AbstractTable table, final AbstractCell cell) {
        super(table, cell);
        this.isPivotalValue = false;
        this.entries = new ArrayList<AbstractCell>();
        this.entries.add(cell);
    }

    private PivotTableHeader(final PivotTableHeader parent, final AbstractCell cell, final boolean isPovitalValue) {
        super(parent.getTable(), cell);
        this.isPivotalValue = isPovitalValue;
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
        return new PivotTableHeader(this, this.getCell(), false);
    }

    public AbstractHeader cloneAsValueHeader() {
        final AbstractCell valueCell = new AbstractCell(DocumentFactory.PIVOT_SUFFIX, 0, 1,
                this.getTable().getClassifier());
        return new PivotTableHeader(this, valueCell, true);
    }

    public boolean isPivotalValue() {
        return this.isPivotalValue;
    }

    public List<AbstractCell> getEntries() {
        return this.entries;
    }

    public void addEntry(final AbstractCell entry) {
        this.entries.add(entry);
    }

    private String name;
    private final boolean isPivotalValue;
    private final ArrayList<AbstractCell> entries;
}
