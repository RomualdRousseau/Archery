package com.github.romualdrousseau.any2json.loader.csv;

public class BatchOfRows {

    private final int position;
    private final int length;

    private BatchOfRows(int position, int length) {
        this.position = position;
        this.length = length;
    }

    public int position() {
        return position;
    }

    public int length() {
        return length;
    }

    public static BatchOfRows of(int position, int length) {
        return new BatchOfRows(position, length);
    }
}
