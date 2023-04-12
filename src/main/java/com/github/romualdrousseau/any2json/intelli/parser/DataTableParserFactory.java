package com.github.romualdrousseau.any2json.intelli.parser;

import com.github.romualdrousseau.any2json.intelli.DataTable;

public interface DataTableParserFactory {

    void disablePivot();

    DataTableParser getInstance(DataTable dataTable);
}
