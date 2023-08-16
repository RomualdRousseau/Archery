package com.github.romualdrousseau.any2json.parser.table;

import com.github.romualdrousseau.any2json.base.DataTable;

public interface DataTableParserFactory {

    DataTableParser getInstance(DataTable dataTable, boolean disablePivot);
}
