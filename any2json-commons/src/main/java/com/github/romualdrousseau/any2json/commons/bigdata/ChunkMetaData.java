package com.github.romualdrousseau.any2json.commons.bigdata;

public class ChunkMetaData {

    private final long position;
    private final int length;

    private ChunkMetaData(final long position, final int length) {
        this.position = position;
        this.length = length;
    }

    public long position() {
        return position;
    }

    public int length() {
        return length;
    }

    public static ChunkMetaData of(final long position, final int length) {
        return new ChunkMetaData(position, length);
    }
}
