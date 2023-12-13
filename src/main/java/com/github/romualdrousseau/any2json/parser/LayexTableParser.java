package com.github.romualdrousseau.any2json.parser;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.TableParser;
import com.github.romualdrousseau.any2json.Model;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.base.BaseTable;
import com.github.romualdrousseau.any2json.base.DataTable;
import com.github.romualdrousseau.any2json.base.MetaTable;
import com.github.romualdrousseau.any2json.header.MetaHeader;
import com.github.romualdrousseau.any2json.parser.table.DataTableGroupSubFooterParserFactory;
import com.github.romualdrousseau.any2json.parser.table.DataTableGroupSubHeaderParserFactory;
import com.github.romualdrousseau.any2json.parser.table.DataTableParser;
import com.github.romualdrousseau.any2json.parser.table.DataTableParserFactory;
import com.github.romualdrousseau.any2json.parser.table.MetaTableParser;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.any2json.layex.Layex;
import com.github.romualdrousseau.any2json.layex.TableLexer;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class LayexTableParser implements TableParser {

    public LayexTableParser(final Model model, final List<String> metaLayexes, final List<String> dataLayexes) {
        this.model = model;
        this.metaLayexes = metaLayexes;
        this.dataLayexes = dataLayexes;

        this.disablePivot = false;
        this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();

        // Update the model with the parser parameters

        this.model.toJSON().setArray("metaLayexes", JSON.arrayOf(this.metaLayexes));
        this.model.toJSON().setArray("dataLayexes", JSON.arrayOf(this.dataLayexes));
    }

    public LayexTableParser(final Model model) {
        this.model = model;
        this.metaLayexes = JSON.<String>streamOf(model.toJSON().getArray("metaLayexes")).toList();
        this.dataLayexes = JSON.<String>streamOf(model.toJSON().getArray("dataLayexes")).toList();

        this.disablePivot = false;
        this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void disablePivot() {
        this.disablePivot = true;
    }

    @Override
    public void setParserOptions(String options) {
        if (options.equals("DataTableGroupSubHeaderParserFactory")) {
            this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        } else if (options.equals("DataTableGroupSubFooterParserFactory")) {
            this.dataTableParserFactory = new DataTableGroupSubFooterParserFactory();
        } else { // Default to DataTableGroupSubHeaderParserFactory
            this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        }
    }

    @Override
    public List<DataTable> getDataTables(final BaseSheet sheet, final List<BaseTable> tables) {
        final List<TableMatcher> dataMatchers = this.getDataMatcherList();
        final ArrayList<DataTable> result = new ArrayList<DataTable>();

        tables.forEach(e -> {
            e.setVisited(false);
        });

        for (final BaseTable table : tables) {
            var foundMatch = false;
            var tryCount = 0;
            do {
                for (final TableMatcher matcher : dataMatchers) {
                    if (!foundMatch && matcher.match(new TableLexer(table, tryCount))) {
                        final DataTable dataTable = new DataTable(table);
                        final DataTableParser parser = this.dataTableParserFactory.getInstance(dataTable,
                                this.disablePivot);
                        this.parseDataTable(table, dataTable, matcher, tryCount, parser, result);

                        if (parser.getSplitRows().size() > 0) {
                            this.splitAllSubTables(sheet, table, matcher, parser, result);
                        }

                        table.setVisited(true);
                        foundMatch = true;
                    }
                }
            }
            while(!foundMatch && ++tryCount < 3);
        }

        return result;
    }

    @Override
    public List<MetaTable> getMetaTables(final BaseSheet sheet, final List<BaseTable> tables) {
        final ArrayList<MetaTable> result = new ArrayList<MetaTable>();

        for (final var table : tables) {
            if (table.isVisited()) {
                continue;
            }

            var foundMatch = false;
            for (final var matcher : this.getMetaMatcherList()) {
                if (!foundMatch && matcher.match(new TableLexer(table, 0))) {
                    final MetaTable metaTable = new MetaTable(table);
                    final MetaTableParser parser = new MetaTableParser(metaTable);
                    this.parseMetaTable(table, metaTable, matcher, parser, result);
                    foundMatch = true;
                }
            }

            if (!foundMatch) {
                final MetaTable metaTable = new MetaTable(table);
                this.convertToMetaHeaders(metaTable, result);
            }

            table.setVisited(true);
        }

        return result;
    }

    public List<TableMatcher> getMetaMatcherList() {
        return this.metaMatchers;
    }

    public void setMetaMatcherList(final List<TableMatcher> matchers) {
        this.metaMatchers = matchers;
    }

    public List<TableMatcher> getDataMatcherList() {
        return this.dataMatchers;
    }

    public void setDataMatcherList(final List<TableMatcher> matchers) {
        this.dataMatchers = matchers;
    }

    private void splitAllSubTables(final BaseSheet sheet, final BaseTable table, final TableMatcher matcher,
            final DataTableParser parser, final List<DataTable> result) {
        var firstRow = -1;
        for (final var splitRow : parser.getSplitRows()) {
            if (firstRow >= 0) {
                final BaseTable subTable = new BaseTable(table, firstRow, table.getFirstRow() + splitRow - 1);
                final DataTable dataTable = new DataTable(subTable);
                this.parseDataTable(subTable, dataTable, matcher, 0, parser, result);
            }
            firstRow = table.getFirstRow() + splitRow;
        }
    }

    private void parseDataTable(final BaseTable table, final DataTable dataTable, final TableMatcher matcher,
            final int rowOffset, final DataTableParser parser, final List<DataTable> result) {
        matcher.match(new TableLexer(table, rowOffset), parser);
        if (parser.getSplitRows().size() > 0) {
            dataTable.adjustLastRow(table.getFirstRow() + parser.getSplitRows().get(0) - 1);
        }
        if (rowOffset > 0) {
            dataTable.setFirstRowOffset(dataTable.getFirstRowOffset() + rowOffset);
        }
        dataTable.ignoreRows().addAll(parser.getIgnoreRows());
        dataTable.setLoadCompleted(true);
        result.add(dataTable);
    }

    private void parseMetaTable(final BaseTable table, final MetaTable metaTable, final TableMatcher matcher,
            final MetaTableParser parser, final List<MetaTable> result) {
        matcher.match(new TableLexer(table, 0), parser);
        result.add(metaTable);
    }

    private void convertToMetaHeaders(final MetaTable metaTable, final List<MetaTable> result) {
        for (final var row : metaTable.rows()) {
            for (final var cell : row.cells()) {
                if (cell.hasValue()) {
                    metaTable.addHeader(new MetaHeader(metaTable, (BaseCell) cell));
                }
            }
        }
        metaTable.setLoadCompleted(true);
        result.add(metaTable);
    }

    private final Model model;
    private final List<String> metaLayexes;
    private final List<String> dataLayexes;
    private boolean disablePivot;
    private DataTableParserFactory dataTableParserFactory;
    private List<TableMatcher> metaMatchers;
    private List<TableMatcher> dataMatchers;
}
