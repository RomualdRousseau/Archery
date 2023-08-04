package com.github.romualdrousseau.any2json.base;

import java.util.List;

import com.github.romualdrousseau.any2json.intelli.CompositeTable;
import com.github.romualdrousseau.any2json.intelli.DataTable;
import com.github.romualdrousseau.any2json.intelli.MetaTable;

public interface BaseSheetParser {

    List<CompositeTable> findAllTables(BaseSheet sheet);

    List<DataTable> getDataTables(BaseSheet sheet, List<CompositeTable> tables);

    List<MetaTable> getMetaTables(BaseSheet sheet, List<CompositeTable> tables);
}
