package com.github.romualdrousseau.archery.transform.op;

import com.github.romualdrousseau.archery.base.BaseSheet;

public class DropRow {

    public static void Apply(final BaseSheet sheet, final int rowIndex) {
        sheet.markRowAsNull(rowIndex);
        sheet.removeAllNullRows();
    }
}
