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
import com.github.romualdrousseau.any2json.parser.table.DataTableParserFactory;
import com.github.romualdrousseau.any2json.parser.table.MetaTableParser;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.any2json.layex.Layex;
import com.github.romualdrousseau.any2json.layex.TableLexer;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class LayexTableParser implements TableParser {

    private final List<String> metaLayexes;
    private final List<String> dataLayexes;

    private Model model;
    private boolean disablePivot;
    private DataTableParserFactory dataTableParserFactory;
    private List<TableMatcher> metaMatchers;
    private List<TableMatcher> dataMatchers;

    public LayexTableParser(final List<String> metaLayexes, final List<String> dataLayexes) {
        this.metaLayexes = metaLayexes;
        this.dataLayexes = dataLayexes;
        this.disablePivot = false;
        this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
    }

    public LayexTableParser(final Model model) {
        this(
            JSON.<String>streamOf(model.toJSON().getArray("metaLayexes")).toList(),
            JSON.<String>streamOf(model.toJSON().getArray("dataLayexes")).toList());
        this.updateModel(model);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void updateModel(final Model model) {
        this.model = model;
        this.model.toJSON().setArray("metaLayexes", JSON.arrayOf(this.metaLayexes));
        this.model.toJSON().setArray("dataLayexes", JSON.arrayOf(this.dataLayexes));
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
                        this.parseDataTable(table, matcher, tryCount, result);
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
                    this.parseMetaTable(table, matcher, result);
                    foundMatch = true;
                }
            }

            if (!foundMatch) {
                this.convertToMetaHeaders(table, result);
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

    private void parseDataTable(final BaseTable table, final TableMatcher matcher,
            final int rowOffset, final List<DataTable> result) {
        final var dataTable = new DataTable(table);
        final var parser = this.dataTableParserFactory.getInstance(dataTable, this.disablePivot);
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

        if (parser.getSplitRows().size() > 0) {
            this.splitAllSubTables(table, matcher, parser.getSplitRows().get(0), result);
        }
    }

    private void splitAllSubTables(final BaseTable table, final TableMatcher matcher, final int splitRow, final List<DataTable> result) {
        final var firstRow = table.getFirstRow() + splitRow;
        if (firstRow < table.getLastRow()) {
            final var nextTable = new BaseTable(table, firstRow, table.getLastRow());
            this.parseDataTable(nextTable, matcher, 0, result);
        }
    }

    private void parseMetaTable(final BaseTable table, final TableMatcher matcher,
            final List<MetaTable> result) {
        final MetaTable metaTable = new MetaTable(table);
        final var parser = new MetaTableParser(metaTable);
        matcher.match(new TableLexer(table, 0), parser);
        result.add(metaTable);
    }

    private void convertToMetaHeaders(final BaseTable table, final List<MetaTable> result) {
        final MetaTable metaTable = new MetaTable(table);
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
}
