package com.github.romualdrousseau.archery;

import com.github.romualdrousseau.archery.base.BaseTable;

public interface ReadingDirection {

    double distanceBetweenTables(final BaseTable table1, final BaseTable table2);
}
