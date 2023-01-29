package com.github.romualdrousseau.any2json.intelli.parser;

import com.github.romualdrousseau.any2json.intelli.DataTable;

public class DataTableGroupSubHeaderParserFactory implements DataTableParserFactory {

    @Override
    public DataTableParser getInstance(DataTable dataTable) {
        return new DataTableGroupSubHeaderParser(dataTable);
    }
}
