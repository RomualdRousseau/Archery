package com.github.romualdrousseau.archery.commons.collections;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;

public class Row implements Iterable<String>, Serializable {

    private final int columnStart;
    private final int columnCount;
    private final String[] data;

    public static final Row Empty = new Row(0);

    public Row(final int columnCount) {
        this.columnStart = 0;
        this.columnCount = columnCount;
        this.data = new String[columnCount];
    }

    private Row(final String[] data) {
        this.columnStart = 0;
        this.columnCount = data.length;
        this.data = data;
    }

    private Row(final int columnStart, final int columnCount, final String[] data) {
        this.columnStart = columnStart;
        this.columnCount = columnCount;
        this.data = data;
    }

    public static Row of(String... data) {
        return new Row(data);
    }

    public Row view(final int columnStart, final int columnCount) {
        Objects.checkFromToIndex(columnStart, columnStart + columnCount - 1, this.columnCount);
        return new Row(columnStart, columnCount, this.data);
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public int size() {
        return this.data.length;
    }

    public String get(final int index) {
        Objects.checkIndex(index, this.columnCount);
        if ((this.columnStart + index) < data.length) {
            return this.data[this.columnStart + index];
        } else {
            return null;
        }
    }

    public Row set(final int index, final String element) {
        assert this != Row.Empty : "Row.Empty is not mutable";
        Objects.checkIndex(index, this.columnCount);
        if ((this.columnStart + index) < data.length) {
            this.data[this.columnStart + index] = element;
        }
        return this;
    }

    @Override
    public Iterator<String> iterator() {
        return new RowIterator(this.data);
    }
}
