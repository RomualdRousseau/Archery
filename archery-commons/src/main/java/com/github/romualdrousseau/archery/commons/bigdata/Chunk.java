package com.github.romualdrousseau.archery.commons.bigdata;

import java.util.ArrayList;
import java.util.List;

public class Chunk {

    private final int batchSize;
    private final List<ChunkMetaData> batches;

    private Row[] rows;

    public Chunk(final int batchSize) {
        this.batchSize = batchSize;
        this.batches = new ArrayList<>();
        this.rows = new Row[this.batchSize];
    }

    public int getBatchSize() {
        return this.batchSize;
    }

    public List<ChunkMetaData> getBatches() {
        return this.batches;
    }

    public Row[] getRows() {
        return this.rows;
    }

    public void setRows(final Row[] rows) {
        this.rows = rows;
    }

    public void setRow(final int idx, final Row row) {
        this.rows[idx] = row;
    }

    public Row getRow(final int idx) {
        return this.rows[idx];
    }
}
