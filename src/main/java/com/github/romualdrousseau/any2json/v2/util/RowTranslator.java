package com.github.romualdrousseau.any2json.v2.util;

import java.util.TreeMap;
import java.util.Map.Entry;

public class RowTranslator {

    public RowTranslator(RowTranslatable translatable) {
        this.translatable = translatable;
        this.rowTranslators = new TreeMap<Integer, Integer>();
        this.lastAccessedRowIndex = -1;
        this.ignoredRowCount = 0;
    }

    public int getIgnoredRowCount() {
        return this.ignoredRowCount;
    }

    public int rebase(int rowIndex) {
        if (rowIndex == this.lastAccessedRowIndex) {
            return lastTranslatedRow;
        }

        int physicalRowIndex = rowIndex;
        for(Entry<Integer, Integer> x : this.rowTranslators.entrySet()) {
            if(x.getKey() > rowIndex) {
                break;
            }
            physicalRowIndex += x.getValue();
        }

        while (translatable.isIgnorableRow(physicalRowIndex)) {
            this.rowTranslators.put(rowIndex, this.rowTranslators.getOrDefault(rowIndex, 0) + 1);
            physicalRowIndex++;
            this.ignoredRowCount++;
        }

        this.lastAccessedRowIndex = rowIndex;
        this.lastTranslatedRow = physicalRowIndex;

        return physicalRowIndex;
    }

    private RowTranslatable translatable;
    private TreeMap<Integer, Integer> rowTranslators;
    private int lastAccessedRowIndex;
    private int lastTranslatedRow;
    private int ignoredRowCount;
}
