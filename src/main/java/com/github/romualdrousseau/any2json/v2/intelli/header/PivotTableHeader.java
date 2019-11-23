package com.github.romualdrousseau.any2json.v2.intelli.header;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;

public class PivotTableHeader extends MetaHeader {

    public class PivotEntry {

        public PivotEntry(AbstractCell cell) {
            this.value = cell.getValue();
            this.colIndex = cell.getColumnIndex();
        }

        public String getValue() {
            return this.value;
        }

        public int getColumnIndex() {
            return this.colIndex;
        }

        private String value;
        private int colIndex;
    }

    public PivotTableHeader(AbstractCell cell) {
        super(cell);
        this.isPivotalKey = true;
        this.entries = new ArrayList<PivotEntry>();
        this.entries.add(new PivotEntry(cell));
    }

    private PivotTableHeader(AbstractCell cell, ArrayList<PivotEntry> entries, boolean isPivotalKey) {
        super(cell);
        this.isPivotalKey = isPivotalKey;
        this.entries = entries;
    }

    @Override
    public String getName() {
        if (this.name == null) {
            String v1 = this.getCell().getValue();
            String v2 = this.getCell().getClassifier().getEntityList().anonymize(v1);
            this.name = this.getCell().getClassifier().getStopWordList().removeStopWords(v2);
            this.name += " " + DocumentFactory.PIVOT_SUFFIX;
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return this.getCell().getValue();
    }

    @Override
    public AbstractHeader clone() {
        return new PivotTableHeader(this.getCell(), this.entries, true);
    }

    public AbstractHeader getValueHeader() {
        AbstractCell valueCell = new AbstractCell(DocumentFactory.PIVOT_SUFFIX, 0, 1, this.getCell().getClassifier());
        return new PivotTableHeader(valueCell, this.entries, false);
    }

    public boolean isPivotalKey() {
        return this.isPivotalKey;
    }

    public List<PivotEntry> getEntries() {
        return this.entries;
    }

    public void addEntry(PivotEntry entry) {
        this.entries.add(entry);
    }

    private String name;
    private boolean isPivotalKey;
    private ArrayList<PivotEntry> entries;
}
