package com.github.romualdrousseau.any2json.transform.op;

import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.commons.strings.StringUtils;

public class DropColumnsWhenFillRatioLessThan {

    public static void Apply(final BaseSheet sheet, final float minRatio) {
        for(int j = 0; j <= sheet.getLastColumnNum(); j++) {
            var emptyCount = sheet.getLastRowNum() + 1;

            final float fillRatio;
            if (emptyCount == 0) {
                fillRatio = 0.0f;
            } else {
                for(int i = 0; i <= sheet.getLastRowNum(); i++) {
                    if(sheet.hasCellDataAt(j, i) && !StringUtils.isFastBlank(sheet.getCellDataAt(j, i))) {
                        emptyCount--;
                    }
                }
                fillRatio = 1.0f - (float) emptyCount / (float) (sheet.getLastRowNum() + 1);
            }

            if (fillRatio <= minRatio) {
                sheet.markColumnAsNull(j);
            }
        }
        sheet.removeAllNullColumns();
    }

    public static void Apply(final BaseSheet sheet, final float minRatio, final int start, final int stop) {
        for(int j = 0; j <= sheet.getLastColumnNum(); j++) {
            var emptyCount = sheet.getLastRowNum() + 1;

            final float fillRatio;
            if (emptyCount == 0) {
                fillRatio = 0.0f;
            } else {
                for(int i = start; i <= stop; i++) {
                    if(sheet.hasCellDataAt(j, i) && !StringUtils.isFastBlank(sheet.getCellDataAt(j, i))) {
                        emptyCount--;
                    }
                }
                fillRatio = 1.0f - (float) emptyCount / (float) (sheet.getLastRowNum() + 1);
            }

            if (fillRatio <= minRatio) {
                sheet.markColumnAsNull(j);
            }
        }
        sheet.removeAllNullColumns();
    }
}
