package com.github.romualdrousseau.any2json.base;

import java.util.Map;
import java.util.LinkedHashMap;

import com.github.romualdrousseau.any2json.DocumentFactory;

public class RowStore extends LinkedHashMap<Integer, BaseRow> {
    private static final long serialVersionUID = 1L;

    public RowStore() {
        super(DocumentFactory.MAX_STORE_ROWS, 0.75F, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, BaseRow> eldest) {
        return this.size() > DocumentFactory.MAX_STORE_ROWS;
    }
}
