package com.github.romualdrousseau.any2json.parser.table;

import com.github.romualdrousseau.any2json.base.DataTable;

public class DataTableGroupSubFooterParserFactory implements DataTableParserFactory {

    @Override
    public DataTableParser getInstance(DataTable dataTable, boolean disablePivot) {
        return new DataTableGroupSubFooterParser(dataTable, disablePivot);
    }
}
