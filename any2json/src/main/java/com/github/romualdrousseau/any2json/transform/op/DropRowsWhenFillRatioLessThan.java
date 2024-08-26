package com.github.romualdrousseau.any2json.transform.op;

import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.shuju.strings.StringUtils;

public class DropRowsWhenFillRatioLessThan {

    public static void Apply(final BaseSheet sheet, final float minRatio) {
        for(int i = 0; i <= sheet.getLastRowNum(); i++) {
            var emptyCount = sheet.getLastColumnNum() + 1;

            final float fillRatio;
            if (emptyCount == 0) {
                fillRatio = 0.0f;
            } else {
                final var lastColumnNum = sheet.getLastColumnNum(i);
                for(int j = 0; j <= lastColumnNum; j++) {
                    if(sheet.hasCellDataAt(j, i) && !StringUtils.isFastBlank(sheet.getCellDataAt(j, i))) {
                        emptyCount--;
                    }
                }
                fillRatio = 1.0f - (float) emptyCount / (float) (sheet.getLastColumnNum() + 1);
            }

            if (fillRatio <= minRatio) {
                sheet.markRowAsNull(i);
            }
        }
        sheet.removeAllNullRows();
    }

    public static void Apply(final BaseSheet sheet, final float minRatio, final int start, final int stop) {
        for(int i = 0; i <= sheet.getLastRowNum(); i++) {
            var emptyCount = sheet.getLastColumnNum() + 1;

            final float fillRatio;
            if (emptyCount == 0) {
                fillRatio = 0.0f;
            } else {
                for(int j = start; j <= stop; j++) {
                    if(sheet.hasCellDataAt(j, i) && !StringUtils.isFastBlank(sheet.getCellDataAt(j, i))) {
                        emptyCount--;
                    }
                }
                fillRatio = 1.0f - (float) emptyCount / (float) (sheet.getLastColumnNum() + 1);
            }

            if (fillRatio <= minRatio) {
                sheet.markRowAsNull(i);
            }
        }
        sheet.removeAllNullRows();
    }
}
