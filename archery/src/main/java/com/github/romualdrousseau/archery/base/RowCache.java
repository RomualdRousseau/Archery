package com.github.romualdrousseau.archery.base;

import org.apache.commons.collections4.map.LRUMap;

import com.github.romualdrousseau.archery.config.Settings;

public class RowCache extends LRUMap<Integer, BaseRow> {

    public RowCache() {
        super(Settings.MAX_STORE_ROWS);
    }
}
