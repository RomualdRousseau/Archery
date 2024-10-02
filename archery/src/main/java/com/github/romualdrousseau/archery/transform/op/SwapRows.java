package com.github.romualdrousseau.archery.transform.op;

import com.github.romualdrousseau.archery.base.BaseSheet;

public class SwapRows {

    public static void Apply(final BaseSheet sheet, final int rowIndex1, final int rowIndex2) {
        sheet.swapRows(rowIndex1, rowIndex2);
    }
}
