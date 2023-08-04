package com.github.romualdrousseau.any2json.intelli;

public interface DataTableParserFactory {

    void disablePivot();

    DataTableParser getInstance(DataTable dataTable);
}
