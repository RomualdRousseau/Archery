package com.github.romualdrousseau.archery.parser;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.archery.parser.table.SimpleTableParser;
import com.github.romualdrousseau.archery.Model;
import com.github.romualdrousseau.archery.TableParser;
import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.base.BaseTable;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.base.MetaTable;
import com.github.romualdrousseau.archery.header.MetaHeader;
import com.github.romualdrousseau.archery.parser.table.DataTableGroupSubFooterParserFactory;
import com.github.romualdrousseau.archery.parser.table.DataTableGroupSubHeaderParserFactory;
import com.github.romualdrousseau.archery.parser.table.DataTableParserFactory;
import com.github.romualdrousseau.archery.parser.table.MetaTableParser;
import com.github.romualdrousseau.archery.layex.Layex;
import com.github.romualdrousseau.archery.layex.TableLexer;
import com.github.romualdrousseau.archery.layex.TableMatcher;

public class LayexTableParser extends SimpleTableParser {

    public static final String GROUP_WITH_SUBHEADER_OPTION = "DataTableGroupSubHeaderParserFactory";
    public static final String GROUP_WITH_SUBFOOTER_OPTION = "DataTableGroupSubFooterParserFactory";
    public static final int TRY_LAYEX_COUNT = 3;

    private final List<String> metaLayexes;
    private final List<String> dataLayexes;

    private DataTableParserFactory dataTableParserFactory;
    private List<TableMatcher> metaMatchers;
    private List<TableMatcher> dataMatchers;

    public LayexTableParser(final List<String> metaLayexes, final List<String> dataLayexes) {
        this.metaLayexes = metaLayexes;
        this.dataLayexes = dataLayexes;
        this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        this.metaMatchers = metaLayexes.stream().map(Layex::new).map(Layex::compile).toList();
        this.dataMatchers = dataLayexes.stream().map(Layex::new).map(Layex::compile).toList();
    }

    public LayexTableParser(final Model model, final String parserOptions) {
        this(
                model.getData().getList("metaLayexes"),
                model.getData().getList("dataLayexes"));
        this.setModel(model);
        this.setParserOptions(parserOptions);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void updateModelData() {
        this.getModel().getData().setList("metaLayexes", this.metaLayexes);
        this.getModel().getData().setList("dataLayexes", this.dataLayexes);
    }

    @Override
    public TableParser setParserOptions(final String parserOptions) {
        if (LayexTableParser.GROUP_WITH_SUBHEADER_OPTION.equals(parserOptions)) {
            this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        } else if (LayexTableParser.GROUP_WITH_SUBFOOTER_OPTION.equals(parserOptions)) {
            this.dataTableParserFactory = new DataTableGroupSubFooterParserFactory();
        } else { // Default to DataTableGroupSubHeaderParserFactory
            this.dataTableParserFactory = new DataTableGroupSubHeaderParserFactory();
        }
        return super.setParserOptions(parserOptions);
    }

    @Override
    public List<DataTable> getDataTables(final BaseSheet sheet, final List<BaseTable> tables) {
        final var matchers = this.getDataMatcherList();
        final var result = new ArrayList<DataTable>();

        for (final var table : tables) {
            table.setVisited(false);

            for (var rowOffset = 0; rowOffset < TRY_LAYEX_COUNT; rowOffset++) {
                final var lexer = new TableLexer(table, rowOffset);
                for (final var matcher : matchers) {
                    if (!table.isVisited()) {
                        table.setVisited(this.parseDataTable(table, lexer, matcher, rowOffset, result));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public List<MetaTable> getMetaTables(final BaseSheet sheet, final List<BaseTable> tables) {
        final var matchers = this.getMetaMatcherList();
        final var result = new ArrayList<MetaTable>();

        for (final var table : tables) {
            if (table.isVisited()) {
                continue;
            }

            final var lexer = new TableLexer(table, 0);
            for (final var matcher : matchers) {
                if (!table.isVisited()) {
                    table.setVisited(this.parseMetaTable(table, lexer, matcher, result));
                }
            }

            if (!table.isVisited() && sheet.isAutoMetaEnabled()) {
                this.convertToMetaHeaders(table, result);
                table.setVisited(true);
            }
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

    private boolean parseDataTable(final BaseTable table, final TableLexer lexer, final TableMatcher matcher,
            final int rowOffset, final List<DataTable> result) {
        final var dataTable = new DataTable(table, rowOffset);
        final var disablePivot = !table.getSheet().isPivotEnabled();
        final var parser = this.dataTableParserFactory.getInstance(dataTable, disablePivot);
        if (!matcher.match(lexer.reset(), parser)) {
            return false;
        }

        if (parser.getSplitRows().size() > 0) {
            dataTable.adjustLastRow(dataTable.getFirstRow() + parser.getSplitRows().get(0) - 1);
        }
        dataTable.ignoreRows().addAll(parser.getIgnoreRows());
        dataTable.setLoadCompleted(true);
        result.add(dataTable);

        if (parser.getSplitRows().size() > 0) {
            return this.splitAllSubTables(table, matcher, rowOffset, parser.getSplitRows().get(0), result);
        }
        return true;
    }

    private boolean splitAllSubTables(final BaseTable table, final TableMatcher matcher, final int rowOffset,
            final int splitRow, final List<DataTable> result) {
        final var firstRow = table.getFirstRow() + rowOffset + splitRow;
        if (firstRow >= table.getLastRow()) {
            return true;
        }
        final var nextTable = new BaseTable(table, firstRow, table.getLastRow());
        final var lexer = new TableLexer(table, 0);
        return this.parseDataTable(nextTable, lexer, matcher, 0, result);
    }

    private boolean parseMetaTable(final BaseTable table, final TableLexer lexer, final TableMatcher matcher,
            final List<MetaTable> result) {
        final MetaTable metaTable = new MetaTable(table);
        final var parser = new MetaTableParser(metaTable);
        if (!matcher.match(lexer.reset(), parser)) {
            return false;
        }
        result.add(metaTable);
        return true;
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
