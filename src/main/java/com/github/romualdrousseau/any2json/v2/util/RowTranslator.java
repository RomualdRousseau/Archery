package com.github.romualdrousseau.any2json.v2.util;

import java.util.TreeMap;
import java.util.Map.Entry;

public class RowTranslator {

    public RowTranslator(RowTranslatable translatable) {
        this.translatable = translatable;
        this.rowTranslators = new TreeMap<Integer, Integer>();
        this.lastLogicalRowIndex = -1;
        this.lastPhysicalRowIndex = -1;
        this.translatedRowCount = 0;
    }

    public int getTranslatedRowCount() {
        return this.translatedRowCount;
    }

    public int translate(int logicalRowIndex) {
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
            while (translatable.isInvalidRow(physicalRowIndex)) {
                this.rowTranslators.put(logicalRowIndex, this.rowTranslators.getOrDefault(logicalRowIndex, 0) + 1);
                physicalRowIndex++;
                this.translatedRowCount++;
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
    private int translatedRowCount;
}
