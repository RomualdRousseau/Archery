package com.github.romualdrousseau.any2json;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Table implements ITable {
    public static boolean IsEmpty(ITable table) {
        return table == null || table.getNumberOfHeaders() == 0;
    }

    public Iterable<TableHeader> headers() {
        return this.headers;
    }

    public int getNumberOfHeaders() {
        return this.headers.size();
    }

    public TableHeader getHeaderAt(int colIndex) {
        return this.headers.get(colIndex);
    }

    public TableHeader getHeaderByTag(String tagName) {
        if (tagName == null) {
            throw new IllegalArgumentException();
        }

        return this.headersByTag.get(tagName);
    }

    public boolean hasHeaders() {
        return this.headers.size() > 0;
    }

    public void clearHeaders() {
        this.headers.clear();
    }

    public Iterable<IRow> rows() {
        return new RowIterable(this);
    }

    public void resetHeaderTags() {
        for(TableHeader header: this.headers) {
            header.resetTag();
        }
        this.headersByTag.clear();
    }

    public void updateHeaderTags(ITagClassifier classifier) {
        assert classifier != null;

        this.resetHeaderTags();

        if (Table.IsEmpty(this)) {
            return;
        }

        for(TableHeader header: this.headers) {
            header.setTagClassifier(classifier);
        }

        for(TableHeader header: this.headers) {
            header.updateTag(false);
        }

        for(TableHeader header: this.headers) {
            header.updateTag(true);
        }

        for(TableHeader header: this.headers) {
            if (header.hasTag() && !header.getTag().isUndefined()) {
                TableHeader head = this.headersByTag.putIfAbsent(header.getTag().getValue(), header);
                if(head != null) {
                    head.chain(header);
                }
            }
        }
    }

    protected ITable addHeader(TableHeader header) {
        this.headers.add(header);
        header.setTable(this);
        return this;
    }

    private ArrayList<TableHeader> headers = new ArrayList<TableHeader>();
    private HashMap<String, TableHeader> headersByTag = new HashMap<String, TableHeader>();
}
