package com.github.romualdrousseau.any2json.v2.util;

public class RowTranslator {

    public RowTranslator(RowTranslatable translatable, int capacity) {
        this.translatable = translatable;

        this.rowTranslators = new byte[capacity];

        for(int i = 0; i < this.rowTranslators.length; i++) {
            this.rowTranslators[i] = 0;
        }
    }

    public int rebase(int colIndex, int rowIndex) {
        if(this.rowTranslators == null) {
            return rowIndex;
        }

        if(rowIndex >= this.rowTranslators.length) {
            return -1;
        }

        int translatedRow = this.rowTranslators[rowIndex] + rowIndex;
        if(translatedRow >= this.rowTranslators.length) {
            return -1;
        }

        if(this.translatable.isSeparatorRow(colIndex, translatedRow)) {
            for(int i = rowIndex; i < this.rowTranslators.length; i++) {
                this.rowTranslators[i]++;
            }
        }

        return this.rowTranslators[rowIndex] + rowIndex;
    }

    private RowTranslatable translatable;
    private byte[] rowTranslators;
}
