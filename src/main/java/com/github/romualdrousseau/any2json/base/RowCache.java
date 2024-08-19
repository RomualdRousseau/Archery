package com.github.romualdrousseau.any2json.base;

import org.apache.commons.collections4.map.LRUMap;

import com.github.romualdrousseau.any2json.config.Settings;

public class RowCache extends LRUMap<Integer, BaseRow> {

    public RowCache() {
        super(Settings.MAX_STORE_ROWS);
    }
}
