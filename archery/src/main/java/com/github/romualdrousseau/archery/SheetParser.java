package com.github.romualdrousseau.archery;

import java.util.List;

import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.base.BaseTable;

public interface SheetParser {

    List<BaseTable> findAllTables(final BaseSheet sheet);
}
