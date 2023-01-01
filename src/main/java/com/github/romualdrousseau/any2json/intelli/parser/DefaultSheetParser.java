package com.github.romualdrousseau.any2json.intelli.parser;

import java.util.ArrayList;
import java.util.List;

import org.python.util.PythonInterpreter;

import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.base.SheetParser;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;
import com.github.romualdrousseau.any2json.intelli.DataTable;
import com.github.romualdrousseau.any2json.intelli.MetaTable;
import com.github.romualdrousseau.any2json.intelli.TransformableSheet;
import com.github.romualdrousseau.any2json.layex.TableLexer;
import com.github.romualdrousseau.any2json.layex.TableMatcher;
import com.github.romualdrousseau.any2json.util.Visitable;

public abstract class DefaultSheetParser implements SheetParser {

    @Override
    public void transformSheet(TransformableSheet sheet) {
        sheet.stichRows();

        final String recipe = sheet.getClassifierFactory().getLayoutClassifier().get().getRecipe();
        if (recipe != null) {
            try(PythonInterpreter pyInterp = new PythonInterpreter()) {
                pyInterp.set("sheet", this);
                pyInterp.exec(recipe);
            }
        }
    }

    @Override
    public List<DataTable> getDataTables(final BaseSheet sheet, final List<CompositeTable> tables) {
        final List<TableMatcher> dataMatchers = sheet.getClassifierFactory().getLayoutClassifier().get().getDataMatcherList();
        final ArrayList<DataTable> result = new ArrayList<DataTable>();

        for (final Visitable e : tables) {
            e.setVisited(false);
        }

        for (final CompositeTable table : tables) {
            boolean foundMatch = false;
            for (int tryCount = 0; tryCount < 3; tryCount++) {
                if (!foundMatch) {
                    for (final TableMatcher matcher : dataMatchers) {
                        if (!foundMatch && matcher.match(new TableLexer(table, tryCount), null)) {
                            final DataTable dataTable = new DataTable(table, matcher, tryCount);
                            result.add(dataTable);
                            if (dataTable.getContext().getSplitRows().size() > 0) {
                                this.splitAllSubTables(table, matcher, dataTable.getContext(), result);
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
    public List<MetaTable> getMetaTables(final BaseSheet sheet, final List<CompositeTable> tables) {
        final List<TableMatcher> metaMatchers = sheet.getClassifierFactory().getLayoutClassifier().get().getMetaMatcherList();
        final ArrayList<MetaTable> result = new ArrayList<MetaTable>();

        for (final CompositeTable table : tables) {
            if (table.isVisited()) {
                continue;
            }

            boolean foundMatch = false;
            for (final TableMatcher matcher : metaMatchers) {
                if (!foundMatch && matcher.match(new TableLexer(table, 0), null)) {
                    result.add(new MetaTable(table, matcher));
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

    private void splitAllSubTables(final CompositeTable table, final TableMatcher layex, final DataTableGroupSubHeaderParser context,
            final List<DataTable> result) {
        int firstRow = -1;
        for (final int splitRow : context.getSplitRows()) {
            if (firstRow >= 0) {
                final CompositeTable subTable = new CompositeTable(table, firstRow, table.getFirstRow() + splitRow - 1);
                result.add(new DataTable(subTable, layex, 0));
            }
            firstRow = table.getFirstRow() + splitRow;
        }
    }
}
