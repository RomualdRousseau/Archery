package com.github.romualdrousseau.archery.event;

import java.util.List;

import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.SheetEvent;
import com.github.romualdrousseau.archery.base.DataTable;

public class DataTableListBuiltEvent extends SheetEvent {

    public DataTableListBuiltEvent(final Sheet source, final List<DataTable> tables) {
        super(source);
        this.dataTables = tables;
    }

    public List<DataTable> getDataTables() {
        return this.dataTables;
    }

    private final List<DataTable> dataTables;
}
