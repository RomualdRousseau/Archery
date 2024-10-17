package com.github.romualdrousseau.archery.readdir;

import com.github.romualdrousseau.archery.ReadingDirection;
import com.github.romualdrousseau.archery.base.BaseTable;

public class GutenbergDiagonal implements ReadingDirection {

    public double distanceBetweenTables(final BaseTable table1, final BaseTable table2) {
        final int vx = table2.getFirstColumn() - table1.getFirstColumn();
        final int vy = table2.getFirstRow() + table2.getHeaderRowOffset() - table1.getLastRow() - 1;
        if (vx >= 0 && vy >= 0) {
            return vx + vy;
        } else {
            return Double.MAX_VALUE;
        }
    }
}
