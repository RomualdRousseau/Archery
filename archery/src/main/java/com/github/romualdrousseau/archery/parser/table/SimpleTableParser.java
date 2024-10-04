package com.github.romualdrousseau.archery.parser.table;

import java.util.Collections;
import java.util.List;

import com.github.romualdrousseau.archery.Model;
import com.github.romualdrousseau.archery.TableParser;
import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.base.BaseTable;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.base.MetaTable;
import com.github.romualdrousseau.archery.header.DataTableHeader;

public class SimpleTableParser implements TableParser {

    public SimpleTableParser() {
        this(null, null);
    }

    public SimpleTableParser(final Model model, final String parserOptions) {
        this.model = model;
        this.parserOptions = parserOptions;
    }

    @Override
    public void close() throws Exception {
    }

    public void updateModelData() {
    }

    @Override
    public Model getModel() {
        return this.model;
    }

    @Override
    public TableParser setModel(final Model model) {
        this.model = model;
        if (this.model != null) {
            this.updateModelData();
        }
        return this;
    }

    @Override
    public String getParserOptions() {
        return parserOptions;
    }

    @Override
    public TableParser setParserOptions(final String parserOptions) {
        this.parserOptions = parserOptions;
        return this;
    }

    @Override
    public List<DataTable> getDataTables(final BaseSheet sheet, final List<BaseTable> tables) {
        final DataTable dataTable = new DataTable(tables.get(0));
        this.parseDataTable(dataTable);
        return List.of(dataTable);
    }

    @Override
    public List<MetaTable> getMetaTables(final BaseSheet sheet, final List<BaseTable> tables) {
        return Collections.emptyList();
    }

    private void parseDataTable(final DataTable table) {
        table.getRowAt(0).cells().forEach(c -> table.addHeader(new DataTableHeader(table, (BaseCell) c)));
        table.setFirstRowOffset(1);
    }

    private Model model;
    private String parserOptions;
}
