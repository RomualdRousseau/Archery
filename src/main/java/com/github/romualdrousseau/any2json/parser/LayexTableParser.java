package com.github.romualdrousseau.any2json.parser;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.TableParser;
import com.github.romualdrousseau.any2json.Model;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.base.BaseTable;
import com.github.romualdrousseau.any2json.base.DataTable;
import com.github.romualdrousseau.any2json.base.MetaTable;
import com.github.romualdrousseau.any2json.parser.table.DataTableGroupSubFooterParserFactory;
import com.github.romualdrousseau.any2json.parser.table.DataTableGroupSubHeaderParserFactory;
import com.github.romualdrousseau.any2json.parser.table.DataTableParser;
import com.github.romualdrousseau.any2json.parser.table.DataTableParserFactory;
import com.github.romualdrousseau.any2json.parser.table.MetaTableParser;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.any2json.layex.Layex;
import com.github.romualdrousseau.any2json.layex.TableLexer;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class LayexTableParser implements TableParser {

    public LayexTableParser(final List<String> metaLayexes, final List<String> dataLayexes) {
        this.metaLayexes = metaLayexes;
        this.dataLayexes = dataLayexes;

        this.disablePivot = false;
        this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
    }

    public LayexTableParser(final Model model) {
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
        }
        else if (options.equals("DataTableGroupSubFooterParserFactory")) {
            this.dataTableParserFactory = new DataTableGroupSubFooterParserFactory();
        }
        else { // Default to DataTableGroupSubHeaderParserFactory
            this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        }
    }

    @Override
    public List<DataTable> getDataTables(final BaseSheet sheet, final List<BaseTable> tables) {
        final List<TableMatcher> dataMatchers = this.getDataMatcherList();
        final ArrayList<DataTable> result = new ArrayList<DataTable>();

        tables.forEach(e -> { e.setVisited(false); });

        for (final BaseTable table : tables) {
            boolean foundMatch = false;
            for (int tryCount = 0; tryCount < 3; tryCount++) {
                if (!foundMatch) {
                    for (final TableMatcher matcher : dataMatchers) {
                        if (!foundMatch && matcher.match(new TableLexer(table, tryCount), null)) {
                            final DataTable dataTable = new DataTable(table);
                            final DataTableParser parser = this.dataTableParserFactory.getInstance(dataTable, this.disablePivot);
                            this.parseDataTable(dataTable, matcher, tryCount, parser);
                            result.add(dataTable);
                            if (parser.getSplitRows().size() > 0) {
                                this.splitAllSubTables(sheet, table, matcher, parser, result);
                            }
                            table.setVisited(true);
                            foundMatch = true;
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public List<MetaTable> getMetaTables(final BaseSheet sheet, final List<BaseTable> tables) {
        final ArrayList<MetaTable> result = new ArrayList<MetaTable>();

        for (final BaseTable table : tables) {
            if (table.isVisited()) {
                continue;
            }

            boolean foundMatch = false;
            for (final TableMatcher matcher : this.getMetaMatcherList()) {
                if (!foundMatch && matcher.match(new TableLexer(table, 0), null)) {
                    final MetaTable metaTable = new MetaTable(table);
                    matcher.match(new TableLexer(metaTable, 0), new MetaTableParser(metaTable));
                    result.add(metaTable);
                    foundMatch = true;
                }
            }
            if (!foundMatch) {
                result.add(new MetaTable(table));
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

    public JSONObject toJSON(final JSONObject root) {
        final JSONObject result = root;
        result.setArray("metaLayexes", JSON.arrayOf(this.metaLayexes));
        result.setArray("dataLayexes", JSON.arrayOf(this.dataLayexes));
        return result;
    }

    private void splitAllSubTables(final BaseSheet sheet, final BaseTable table, final TableMatcher matcher,
            final DataTableParser parser, final List<DataTable> result) {
        int firstRow = -1;
        for (final int splitRow : parser.getSplitRows()) {
            if (firstRow >= 0) {
                final BaseTable subTable = new BaseTable(table, firstRow, table.getFirstRow() + splitRow - 1);
                final DataTable dataTable = new DataTable(subTable);
                this.parseDataTable(dataTable, matcher, 0, parser);
                result.add(dataTable);
            }
            firstRow = table.getFirstRow() + splitRow;
        }
    }

    private void parseDataTable(final DataTable table, final TableMatcher matcher, final int rowOffset, final DataTableParser parser) {
        matcher.match(new TableLexer(table, rowOffset), parser);
        if (parser.getSplitRows().size() > 0) {
            table.adjustLastRow(table.getFirstRow() + parser.getSplitRows().get(0) - 1, true);
        }
        if (rowOffset > 0) {
            table.setFirstRowOffset(table.getFirstRowOffset() + rowOffset);
        }
        table.ignoreRows().addAll(parser.getIgnoreRows());
        table.setLoadCompleted(true);
    }

    private final List<String> metaLayexes;
    private final List<String> dataLayexes;
    private boolean disablePivot;
    private DataTableParserFactory dataTableParserFactory;
    private List<TableMatcher> metaMatchers;
    private List<TableMatcher> dataMatchers;
}
