package com.github.romualdrousseau.any2json.v2.util;

import java.util.TreeMap;
import java.util.Map.Entry;

public class RowTranslator {

    public RowTranslator(RowTranslatable translatable) {
        this.translatable = translatable;
        this.rowTranslators = new TreeMap<Integer, Integer>();
        this.lastLogicalRowIndex = -1;
        this.lastPhysicalRowIndex = -1;
        this.ignoredRowCount = 0;
    }

    public int getIgnoredRowCount() {
        return this.ignoredRowCount;
    }

    public int rebase(int logicalRowIndex) {
        if (this.lastLogicalRowIndex == logicalRowIndex) {
            return this.lastPhysicalRowIndex;
        }

        int physicalRowIndex = logicalRowIndex;
        for(Entry<Integer, Integer> x : this.rowTranslators.entrySet()) {
            if(x.getKey() > logicalRowIndex) {
                break;
            }
            physicalRowIndex += x.getValue();
        }

        if(this.rowTranslators.size() == 0 || logicalRowIndex > this.rowTranslators.lastKey()) {
            while (translatable.isIgnorableRow(physicalRowIndex)) {
                this.rowTranslators.put(logicalRowIndex, this.rowTranslators.getOrDefault(logicalRowIndex, 0) + 1);
                physicalRowIndex++;
                this.ignoredRowCount++;
            }
        }

        this.lastLogicalRowIndex = logicalRowIndex;
        this.lastPhysicalRowIndex = physicalRowIndex;

        return physicalRowIndex;
    }

    private RowTranslatable translatable;
    private TreeMap<Integer, Integer> rowTranslators;
    private int lastLogicalRowIndex;
    private int lastPhysicalRowIndex;
    private int ignoredRowCount;
}
