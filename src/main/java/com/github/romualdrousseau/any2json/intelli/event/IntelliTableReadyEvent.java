package com.github.romualdrousseau.any2json.intelli.event;

import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.SheetEvent;
import com.github.romualdrousseau.any2json.Table;

public class IntelliTableReadyEvent extends SheetEvent {

    public IntelliTableReadyEvent(final Sheet source, final Table table) {
        super(source);
        this.table = table;
    }

    public Table getTable() {
        return this.table;
    }
    private final Table table;
}
