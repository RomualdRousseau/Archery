package com.github.romualdrousseau.archery.transform.op;

import com.github.romualdrousseau.archery.base.BaseSheet;

public class DropColumn {

    public static void Apply(final BaseSheet sheet, final int colIndex) {
        sheet.markColumnAsNull(colIndex);
        sheet.removeAllNullColumns();
    }
}
