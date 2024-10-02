package com.github.romualdrousseau.archery.parser.table;

import com.github.romualdrousseau.archery.base.DataTable;

public class DataTableGroupSubHeaderParserFactory implements DataTableParserFactory {

    @Override
    public DataTableParser getInstance(DataTable dataTable, boolean disablePivot) {
        return new DataTableGroupSubHeaderParser(dataTable, disablePivot);
    }
}
