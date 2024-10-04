package com.github.romualdrousseau.archery.parser.table;

import com.github.romualdrousseau.archery.base.DataTable;

public interface DataTableParserFactory {

    DataTableParser getInstance(DataTable dataTable, boolean disablePivot);
}
