package com.github.romualdrousseau.any2json.intelli.parser.sheet;

import java.util.List;

import com.github.romualdrousseau.any2json.event.AllTablesExtractedEvent;
import com.github.romualdrousseau.any2json.event.DataTableListBuiltEvent;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;
import com.github.romualdrousseau.any2json.intelli.DataTable;
import com.github.romualdrousseau.any2json.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.intelli.IntelliSheetParser;

public class StructuredSheetParser extends IntelliSheetParser {

    @Override
    public CompositeTable parseAllTables(final IntelliSheet sheet) {
        if (!this.transformSheet(sheet)) {
            return null;
        }

        final List<CompositeTable> tables = List
                .of(new CompositeTable(sheet, 0, 0, sheet.getLastColumnNum(), sheet.getLastRowNum()));
        if (!sheet.notifyStepCompleted(new AllTablesExtractedEvent(sheet, tables))) {
            return null;
        }

        final List<DataTable> dataTables = List.of(new DataTable(tables.get(0)));
        if (!sheet.notifyStepCompleted(new DataTableListBuiltEvent(sheet, dataTables))) {
            return null;
        }

        return dataTables.get(0);
    }
}
