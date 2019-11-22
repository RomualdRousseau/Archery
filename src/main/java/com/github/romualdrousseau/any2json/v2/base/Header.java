package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.IHeader;

public abstract class Header implements IHeader {

    public Header(Cell cell, int colIndex) {
        this.cell = cell;
        this.colIndex = colIndex;
    }

    public int getColumnIndex() {
        return this.colIndex;
    }

    public void setColumnIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public Cell getCell() {
        return this.cell;
    }

    public boolean equals(Header o) {
        return this.getName().equals(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Header && this.equals((Header) o);
    }

    public abstract Header clone();

    private Cell cell;
    private int colIndex;
}
