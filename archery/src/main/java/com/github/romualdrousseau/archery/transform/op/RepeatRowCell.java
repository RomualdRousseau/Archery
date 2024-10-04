package com.github.romualdrousseau.archery.transform.op;

import com.github.romualdrousseau.archery.base.BaseSheet;

public class RepeatRowCell {

    public static void Apply(final BaseSheet sheet, final int rowIndex) {
        final var lastColumnNum = sheet.getLastColumnNum(rowIndex);
        var lastColumn = -1;
        for(int i = 0; i <= lastColumnNum; i++) {
            if(sheet.hasCellDataAt(i, rowIndex) && !sheet.getCellDataAt(i, rowIndex).isBlank()) {
                lastColumn = i;
            } else if (lastColumn >= 0) {
                sheet.patchCell(lastColumn, rowIndex, i, rowIndex, null);
            }
        }
    }
}
