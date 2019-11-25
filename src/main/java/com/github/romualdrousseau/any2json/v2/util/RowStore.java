package com.github.romualdrousseau.any2json.v2.util;

import java.util.Map;
import java.util.LinkedHashMap;

import com.github.romualdrousseau.any2json.v2.base.BaseRow;

public class RowStore extends LinkedHashMap<Integer, BaseRow> {
    private static final long serialVersionUID = 1L;

    public static final int MAX_ROWS = 50000;

    public RowStore() {
        super(RowStore.MAX_ROWS, 0.75F, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, BaseRow> eldest) {
        return this.size() > RowStore.MAX_ROWS;
    }
}
