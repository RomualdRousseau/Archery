package com.github.romualdrousseau.any2json.transform.op;

import org.apache.commons.collections4.map.LRUMap;

import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.config.Settings;
import com.github.romualdrousseau.shuju.strings.StringFuzzy;

public class StitchRows {

    public static void Apply(final BaseSheet sheet) {
        final var cachedHashes = new LRUMap<Integer, String>();
        final var lastRowNum = sheet.getLastRowNum();
        for(int i = 0; i <= lastRowNum; i++) {
            if (StitchRows.isStichedRow(sheet, i, cachedHashes)) {
                sheet.markRowAsNull(i);
            }
        }
        sheet.removeAllNullRows();
    }

    private static boolean isStichedRow(final BaseSheet sheet, final int rowIndex, LRUMap<Integer, String> cachedHashes) {
        if (rowIndex <= 0 || rowIndex >= sheet.getSheetStore().getLastRowNum()) {
            return false;
        }
        final var hash = cachedHashes.computeIfAbsent(rowIndex, x -> StitchRows.getRowHash(sheet, x));
        // Keep non empty rows
        if (!hash.isEmpty()) {
            return false;
        }
        // Test if the previous and next rows can be "stiched"
        final var hashPrev = cachedHashes.computeIfAbsent(rowIndex - 1, x -> StitchRows.getRowHash(sheet, x));
        final var hashNext = cachedHashes.computeIfAbsent(rowIndex + 1, x -> StitchRows.getRowHash(sheet, x));
        return StringFuzzy.Hamming(hashPrev, hashNext) >= Settings.DEFAULT_RATIO_SIMILARITY;
    }

    private static String getRowHash(final BaseSheet sheet, final int rowIndex) {
        var hash = "";
        var countEmptyCells = 0;
        final var lastColumnNum = sheet.getSheetStore().getLastColumnNum(rowIndex);
        for (int i = 0; i <= lastColumnNum;) {
            final var value = sheet.getSheetStore().getCellDataAt(i, rowIndex);
            if (value != null) {
                if (value.isEmpty()) {
                    hash += "s";
                    countEmptyCells++;
                }
                else {
                    hash += sheet.getDocument().getModel().toEntityValue(value).map(x -> "e").orElse("v");
                }
            }
            i += sheet.getSheetStore().getNumberOfMergedCellsAt(i, rowIndex);
        }
        if (countEmptyCells == hash.length()) {
            hash = "";
        }

        return hash;
    }
}
