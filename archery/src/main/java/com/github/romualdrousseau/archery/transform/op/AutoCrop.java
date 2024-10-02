package com.github.romualdrousseau.archery.transform.op;

import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class AutoCrop {

    public static void Apply(final BaseSheet sheet, final float minRatio) {
        AutoCrop._cropLeft(sheet, minRatio);
        sheet.removeAllNullColumns();
        AutoCrop._cropRight(sheet, minRatio);
        sheet.removeAllNullColumns();

        AutoCrop._cropTop(sheet, minRatio);
        sheet.removeAllNullRows();
        AutoCrop._cropBottom(sheet, minRatio);
        sheet.removeAllNullRows();
    }

    public static void _cropLeft(final BaseSheet sheet, final float minRatio) {
        for (int j = 0; j <= sheet.getLastColumnNum(); j++) {
            var emptyCount = sheet.getLastRowNum() + 1;

            final float fillRatio;
            if (emptyCount == 0) {
                fillRatio = 0.0f;
            } else {
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    if (sheet.hasCellDataAt(j, i) && !StringUtils.isFastBlank(sheet.getCellDataAt(j, i))) {
                        emptyCount--;
                    }
                }
                fillRatio = 1.0f - (float) emptyCount / (float) (sheet.getLastRowNum() + 1);
            }

            if (fillRatio <= minRatio) {
                sheet.markColumnAsNull(j);
            } else {
                return;
            }
        }
    }

    public static void _cropRight(final BaseSheet sheet, final float minRatio) {
        for (int j = sheet.getLastColumnNum(); j >= 0; j--) {
            int emptyCount = sheet.getLastRowNum() + 1;

            final float fillRatio;
            if (emptyCount == 0) {
                fillRatio = 0.0f;
            } else {
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    if (sheet.hasCellDataAt(j, i) && !StringUtils.isFastBlank(sheet.getCellDataAt(j, i))) {
                        emptyCount--;
                    }
                }
                fillRatio = 1.0f - (float) emptyCount / (float) (sheet.getLastRowNum() + 1);
            }

            if (fillRatio <= minRatio) {
                sheet.markColumnAsNull(j);
            } else {
                return;
            }
        }
    }

    public static void _cropTop(final BaseSheet sheet, final float minRatio) {
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            int emptyCount = sheet.getLastColumnNum() + 1;

            final float fillRatio;
            if (emptyCount == 0) {
                fillRatio = 0.0f;
            } else {
                final var lastColumnNum = sheet.getLastColumnNum(i);
                for (int j = 0; j <= lastColumnNum; j++) {
                    if (sheet.hasCellDataAt(j, i) && !StringUtils.isFastBlank(sheet.getCellDataAt(j, i))) {
                        emptyCount--;
                    }
                }
                fillRatio = 1.0f - (float) emptyCount / (float) (sheet.getLastColumnNum() + 1);
            }

            if (fillRatio <= minRatio) {
                sheet.markRowAsNull(i);
            } else {
                return;
            }
        }
    }

    public static void _cropBottom(final BaseSheet sheet, final float minRatio) {
        for (int i = sheet.getLastRowNum(); i >= 0; i--) {
            int emptyCount = sheet.getLastColumnNum() + 1;

            final float fillRatio;
            if (emptyCount == 0) {
                fillRatio = 0.0f;
            } else {
                final var lastColumnNum = sheet.getLastColumnNum(i);
                for (int j = 0; j <= lastColumnNum; j++) {
                    if (sheet.hasCellDataAt(j, i) && !StringUtils.isFastBlank(sheet.getCellDataAt(j, i))) {
                        emptyCount--;
                    }
                }
                fillRatio = 1.0f - (float) emptyCount / (float) (sheet.getLastColumnNum() + 1);
            }

            if (fillRatio <= minRatio) {
                sheet.markRowAsNull(i);
            } else {
                return;
            }
        }
    }
}
