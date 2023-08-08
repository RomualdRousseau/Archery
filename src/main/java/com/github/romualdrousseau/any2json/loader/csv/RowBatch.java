package com.github.romualdrousseau.any2json.loader.csv;

public class RowBatch {

    private final int position;
    private final int length;

    private RowBatch(int position, int length) {
        this.position = position;
        this.length = length;
    }

    public int position() {
        return position;
    }

    public int length() {
        return length;
    }

    public static RowBatch of(int position, int length) {
        return new RowBatch(position, length);
    }
}
