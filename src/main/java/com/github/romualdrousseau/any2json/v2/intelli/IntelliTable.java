package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.base.AbstractSheet;
import com.github.romualdrousseau.any2json.v2.intelli.header.IntelliHeader;

import java.util.HashMap;

import com.github.romualdrousseau.any2json.ITagClassifier;

public class IntelliTable extends AbstractTable {

    public IntelliTable(final ITagClassifier classifier) {
        this(null, 0, 0, 0, 0, classifier);
    }

    public IntelliTable(final IntelliTable parent) {
        super(parent);
        this.classifier = parent.classifier;
    }

    public IntelliTable(final IntelliTable parent, final int firstRow, final int lastRow) {
        super(parent, firstRow, lastRow);
        this.classifier = parent.classifier;
    }

    public IntelliTable(AbstractSheet sheet, int firstColumn, int firstRow, int lastColumn, int lastRow, ITagClassifier classifier) {
        super(sheet, firstColumn, firstRow, lastColumn, lastRow);
        assert (classifier != null) : "classifier must be defined";
        this.classifier = classifier;
    }

    @Override
    public ITagClassifier getClassifier() {
        return this.classifier;
    }

    @Override
    public void updateHeaderTags() {
        for (Header header : this.headers()) {
            ((IntelliHeader) header).resetTag();
        }

        for (Header header : this.headers()) {
            ((IntelliHeader) header).updateTag(false);
        }

        for (Header header : this.headers()) {
            ((IntelliHeader) header).updateTag(true);
        }

        for (Header header : this.headers()) {
            if (header.hasTag() && !header.getTag().isUndefined()) {
                Header head = this.headersByTag.putIfAbsent(header.getTag().getValue(), header);
                if (head != null) {
                    ((IntelliHeader) head).mergeTo((IntelliHeader) header);
                }
            }
        }
    }

    @Override
    public int getNumberOfHeaderTags() {
        return this.headersByTag.size();
    }

    @Override
    public Iterable<Header> headerTags() {
        return this.headersByTag.values();
    }

    private final ITagClassifier classifier;
    private final HashMap<String, Header> headersByTag = new HashMap<String, Header>();
}
