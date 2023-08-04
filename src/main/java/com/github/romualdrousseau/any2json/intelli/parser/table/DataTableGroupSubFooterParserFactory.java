package com.github.romualdrousseau.any2json.intelli.parser.table;

import com.github.romualdrousseau.any2json.intelli.DataTable;
import com.github.romualdrousseau.any2json.intelli.DataTableParser;
import com.github.romualdrousseau.any2json.intelli.DataTableParserFactory;

public class DataTableGroupSubFooterParserFactory implements DataTableParserFactory {

    private boolean disablePivot = false;

    @Override
    public void disablePivot() {
        this.disablePivot = true;
    }

    @Override
    public DataTableParser getInstance(DataTable dataTable) {
        return new DataTableGroupSubFooterParser(dataTable, this.disablePivot);
    }
}
