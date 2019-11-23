package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.Header;

public abstract class AbstractHeader implements Header {

    public AbstractHeader(AbstractCell cell, int colIndex) {
        this.cell = cell;
        this.colIndex = colIndex;
    }

    public int getColumnIndex() {
        return this.colIndex;
    }

    public void setColumnIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public AbstractCell getCell() {
        return this.cell;
    }

    public boolean equals(AbstractHeader o) {
        return this.getName().equals(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AbstractHeader && this.equals((AbstractHeader) o);
    }

    public abstract AbstractHeader clone();

    private AbstractCell cell;
    private int colIndex;
}
