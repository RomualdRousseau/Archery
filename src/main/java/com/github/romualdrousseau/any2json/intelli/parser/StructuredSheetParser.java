package com.github.romualdrousseau.any2json.intelli.parser;

import java.util.List;
import java.util.LinkedList;

import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.base.BaseSheetParser;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;
import com.github.romualdrousseau.any2json.intelli.DataTable;
import com.github.romualdrousseau.any2json.intelli.MetaTable;
import com.github.romualdrousseau.any2json.intelli.TransformableSheet;

public class StructuredSheetParser implements BaseSheetParser {

    @Override
    public void transformSheet(TransformableSheet sheet) {
    }

    @Override
    public List<CompositeTable> findAllTables(BaseSheet sheet) {
        final List<CompositeTable> tables = new LinkedList<CompositeTable>();
        tables.add(new CompositeTable(sheet, 0, 0, sheet.getLastColumnNum(), sheet.getLastRowNum()));
        return tables;
    }

    @Override
    public List<DataTable> getDataTables(BaseSheet sheet, List<CompositeTable> tables) {
        final List<DataTable> dataTables = new LinkedList<DataTable>();
        dataTables.add(new DataTable(tables.get(0)));
        return dataTables;
    }

    @Override
    public List<MetaTable> getMetaTables(BaseSheet sheet, List<CompositeTable> tables) {
        return new LinkedList<MetaTable>();
    }
}
