package com.github.romualdrousseau.archery.event;

import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.SheetEvent;
import com.github.romualdrousseau.archery.Table;

public class TableReadyEvent extends SheetEvent {

    public TableReadyEvent(final Sheet source, final Table table) {
        super(source);
        this.table = table;
    }

    public Table getTable() {
        return this.table;
    }
    private final Table table;
}
