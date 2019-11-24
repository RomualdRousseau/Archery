package com.github.romualdrousseau.any2json.v2.intelli.event;

import com.github.romualdrousseau.any2json.v2.Sheet;
import com.github.romualdrousseau.any2json.v2.SheetEvent;
import com.github.romualdrousseau.any2json.v2.Table;

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
